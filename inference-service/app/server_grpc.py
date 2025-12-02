import os
import sys
import grpc
from concurrent import futures

from business_logic import (
    load_model,
    preprocess_data,
    apply_heuristic_adjustment,
    get_cost_sensitive_action
)

# Configuração de paths para o gRPC
current_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.append(current_dir)
sys.path.append(os.path.join(current_dir, 'grpc_generated'))

# Importa os módulos gRPC gerados
# Pode apresentar erros na IDE, mas o código é executado corretamente no container Docker
import fraud_detection_pb2
import fraud_detection_pb2_grpc

class FraudDetectionServicer(fraud_detection_pb2_grpc.FraudDetectionServiceServicer):

    def __init__(self):
        print(">>> [gRPC] Inicializando servidor...")
        # Carrega o modelo
        self.model = load_model()

    def PredictFraud(self, request, context):
        print(f">>> [gRPC] Requisição recebida. UserID: {request.user_id}")

        if self.model is None:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details('Modelo ML não está carregado no servidor.')
            return fraud_detection_pb2.AnalysisResponse()

        # Pré-processamento dos dados
        features_df = preprocess_data(
            request.value,
            request.transaction_count,
            request.average_amount,
            request.current_transaction_country
        )

        # Predição com XGBoost
        fraud_probability = 0.0
        try:
            # Pega a probabilidade da classe 1 (fraude)
            fraud_probability = float(self.model.predict_proba(features_df)[0][1])
        except Exception as e:
            print(f">>> [gRPC] Erro ao executar predição XGBoost: {e}")
            # Em caso de erro, mantém 0.0 para não travar o fluxo
            fraud_probability = 0.0

        # Ajuste heurístico
        fraud_probability = apply_heuristic_adjustment(
            fraud_probability,
            request.value,
            request.average_amount,
            request.transaction_count
        )

        # Decisão de negócio
        action = get_cost_sensitive_action(fraud_probability, request.value)

        print(f">>> [gRPC] Resultado enviado: Score {fraud_probability:.4f} -> {action}")

        # Retorna o objeto gRPC
        return fraud_detection_pb2.AnalysisResponse(
            risk_score=fraud_probability,
            recommended_action=action
        )

def serve():
    """
    Inicia o servidor gRPC.
    """
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    fraud_detection_pb2_grpc.add_FraudDetectionServiceServicer_to_server(FraudDetectionServicer(), server)

    server.add_insecure_port('0.0.0.0:50051')
    print(">>> Servidor gRPC de Detecção de Fraude rodando na porta 50051...")
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    serve()
