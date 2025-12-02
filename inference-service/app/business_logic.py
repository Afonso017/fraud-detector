import xgboost as xgb
import pandas as pd

# Constante global para o arquivo do modelo
MODEL_FILE = "xgboost_model.json"


def load_model():
    """
    Carrega o modelo XGBoost do disco para a memória.
    """
    model = xgb.XGBClassifier()
    try:
        model.load_model(MODEL_FILE)
        print(f">>> [MAIN] Modelo de ML '{MODEL_FILE}' carregado com sucesso.")
        return model
    except Exception as e:
        print(f">>> [MAIN] Erro crítico ao carregar o modelo. Arquivo não encontrado... \n{e}")
        return None


def preprocess_data(
        value: float,
        transaction_count: int,
        average_amount: float,
        country_code: str
) -> pd.DataFrame:
    """
    Transforma os dados brutos da requisição em um DataFrame Pandas formatado para o modelo.
    """
    clean_average_amount = max(0.0, average_amount)
    clean_value = abs(value)

    # 'is_foreign_country' é calculado baseado no país atual da transação
    # Se for diferente de "BRA", considera internacional (risco maior)
    is_foreign = 0 if country_code == "BRA" else 1

    # Cria um dicionário com os dados
    data = {
        'value': [clean_value],
        'transaction_count': [transaction_count],
        'average_amount': [clean_average_amount],
        'is_foreign_country': [is_foreign]
    }

    # Define a ordem exata das colunas que o modelo espera
    FEATURES = ['value', 'transaction_count', 'average_amount', 'is_foreign_country']

    return pd.DataFrame(data, columns=FEATURES)


def apply_heuristic_adjustment(
        fraud_probability: float,
        value: float,
        average_amount: float,
        count: int
) -> float:
    """
    Aplica regras de negócio (heurísticas) para ajustar a probabilidade bruta do modelo.
    """
    # Se o usuário tem histórico e o valor é consistente, mais ou menos 20% da média,
    # a chance de fraude é reduzida pela metade.
    if count >= 1 and average_amount > 0:
        ratio = value / average_amount

        if 0.8 <= ratio <= 1.2:
            print(f">>> [MAIN] Bônus de consistência aplicado, valor próximo da média.")
            return fraud_probability * 0.5

    return fraud_probability


def get_cost_sensitive_action(
        probability_of_fraud: float,
        transaction_value: float
) -> str:
    """
    Implementa a lógica do risco mínimo de Bayes (Bayes Minimum Risk).
    O objetivo é tomar decisões que minimizam o custo financeiro.
    """
    # Custo de um falso positivo: bloquear uma transação legítima
    # Este é um valor de negócio
    COST_FP = 200.0

    # Custo de um falso negativo: deixar uma fraude passar
    # É o valor total da transação
    COST_FN = transaction_value

    # A ação é fraudulenta se o risco de não agir for maior que o risco de agir
    # Classifica como fraude se:
    # probabilidade_de_fraude * custo_falso_negativo > probabilidade_de_não_fraude * custo_falso_positivo
    threshold = COST_FP / (COST_FN + COST_FP) if (COST_FN + COST_FP) > 0 else 1.0

    # Definimos um segundo limite mais agressivo para recusa direta
    decline_threshold = 0.90  # Limite fixo para transações de altíssimo risco
    safe_threshold = 0.15     # Limite fixo para transações de baixo risco

    print(
        f">>> [MAIN] Valor: R${transaction_value:.2f}, Limite dinâmico: {threshold:.4f}, "
        f"Score final: {probability_of_fraud:.4f}"
    )

    if probability_of_fraud > decline_threshold:
        return "DECLINE"

    if probability_of_fraud < safe_threshold:
        return "APPROVE"

    if probability_of_fraud > threshold:
        return "REVIEW"  # Precisa de análise humana

    return "APPROVE"
