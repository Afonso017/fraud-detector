from contextlib import asynccontextmanager
from fastapi import FastAPI
from pydantic import BaseModel, Field
import numpy as np


@asynccontextmanager
async def lifespan(app: FastAPI):
    get_model()
    print(">>> Aplicação iniciada e pronta para receber requisições")
    yield


app = FastAPI(lifespan=lifespan)
model = None

# Esta função simula o carregamento de um modelo treinado
def get_model():
    global model
    if model is None:
        # Sempre retorna uma probabilidade fixa ou baseada em regras simples
        print(">>> Modelo de ML não encontrado. Carregando um simulador.")
        model = "SIMULATOR"
    return model


# Modelos de requisição e resposta
class AnalysisRequest(BaseModel):
    user_id: str = Field(..., alias='userId')
    value: float = Field(..., alias='value')
    transaction_count: int = Field(..., alias='transactionCount')
    average_amount: float = Field(..., alias='averageAmount')
    last_transaction_country: str = Field(..., alias='lastTransactionCountry')

class AnalysisResponse(BaseModel):
    risk_score: float = Field(..., alias='riskScore')
    recommended_action: str = Field(..., alias='recommendedAction')


# Função que simula a predição do modelo de ML, por enquanto, usada apenas para testes
def predict_proba_with_simulator(request: AnalysisRequest) -> float:
    """
    Esta função simula o que um modelo de previsão de fraude faria:
    1. Recebe os dados da requisição.
    2. Transforma esses dados em features numéricas.
    3. Usa o modelo para prever a probabilidade de fraude (um score de 0.0 a 1.0).
    """
    score = 0.0

    # Lógica baseada em regras para simular a predição do modelo
    if request.average_amount > 0:
        if request.value > (request.average_amount * 2):
            score += 0.4
    elif request.value > 1000: # Se não há média, valor alto já é suspeito
        score += 0.3

    if request.last_transaction_country != "BRA":
        score += 0.3

    if request.transaction_count < 2:
        score += 0.2

    # Normalização e um pouco de ruído para parecer mais real
    final_score = min(score, 1.0)
    final_score = max(0.01, final_score * (1 + np.random.uniform(-0.05, 0.05)))

    return min(final_score, 1.0)


def get_cost_sensitive_action(probability_of_fraud: float, transaction_value: float) -> str:
    """
    Implementa a lógica do risco mínimo de Bayes (Bayes Minimum Risk).
    O objetivo é tomar decisões que minimizam o custo financeiro.
    """
    # Custo de um falso positivo: bloquear uma transação legítima
    # Este é um valor de negócio, é fixo em 2 unidades monetárias
    COST_FP = 2.0

    # Custo de um falso negativo: deixar uma fraude passar
    # É o valor total da transação
    COST_FN = transaction_value

    # A ação é fraudulenta se o risco de não agir for maior que o risco de agir
    # Classifica como fraude se:
    # probabilidade_de_fraude * custo_falso_negativo > probabilidade_de_não_fraude * custo_falso_positivo
    # Ou seja:
    # probabilidade_de_fraude > custo_falso_positivo / (custo_falso_negativo + custo_falso_positivo)
    threshold = COST_FP / (COST_FN + COST_FP) if (COST_FN + COST_FP) > 0 else 1.0

    # Definimos um segundo limite mais agressivo para recusa direta
    decline_threshold = 0.90 # Limite fixo para transações de altíssimo risco

    print(f">>> Valor da Transação: R${transaction_value:.2f}, Limite de Risco Mínimo: {threshold:.4f}")

    if probability_of_fraud > decline_threshold:
        return "DECLINE"
    if probability_of_fraud > threshold:
        return "REVIEW" # Precisa de análise humana

    return "APPROVE"

@app.post("/predict", response_model=AnalysisResponse)
def predict_fraud(request: AnalysisRequest):
    print(f">>> Análise de risco solicitada para: {request.model_dump(by_alias=True)}")

    ml_model = get_model()

    # Obtém a probabilidade de fraude do modelo de ML
    fraud_probability = predict_proba_with_simulator(request)

    # Usa a teoria do Bayes Minimum Risk para decidir a ação
    action = get_cost_sensitive_action(fraud_probability, request.value)

    print(f">>> Score de Probabilidade: {fraud_probability:.4f}, Ação Recomendada: {action}")

    return AnalysisResponse(riskScore=fraud_probability, recommendedAction=action)

@app.get("/health")
def health_check():
    return {"status": "ok"}