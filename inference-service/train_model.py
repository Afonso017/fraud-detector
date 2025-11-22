import pandas as pd
import numpy as np
import xgboost as xgb
from sklearn.model_selection import train_test_split

print("Iniciando a geração de dados sintéticos...")

N_SAMPLES = 100000
np.random.seed(42)

# Gerar dados base
probs_low = [0.1] * 5
probs_high = [(1.0 - sum(probs_low)) / 45] * 45
probs_final = probs_low + probs_high

data = {
    'value': np.random.lognormal(mean=3.5, sigma=1.0, size=N_SAMPLES),
    'transaction_count': np.random.choice(np.arange(0, 50), size=N_SAMPLES, p=probs_final),
    'average_amount': np.random.lognormal(mean=3.5, sigma=1.2, size=N_SAMPLES),
    'is_foreign_country': np.random.choice([0, 1], size=N_SAMPLES, p=[0.95, 0.05])
}
df = pd.DataFrame(data)

# Ajustes iniciais
df.loc[df['transaction_count'] == 0, 'average_amount'] = 0
df['is_fraud'] = 0

# Injeção de padrões de comportamento fraudulento

# 30% dos dados para bons clientes, usuários com histórico e com valores de transação dentro da média
n_good = int(N_SAMPLES * 0.3)
indices_good = np.random.choice(df.index, size=n_good, replace=False)

# Para esses, o valor é muito próximo da média
df.loc[indices_good, 'average_amount'] = np.random.uniform(50, 500, size=n_good)
df.loc[indices_good, 'value'] = df.loc[indices_good, 'average_amount'] * np.random.uniform(0.9, 1.1, size=n_good)
df.loc[indices_good, 'transaction_count'] = np.random.randint(5, 100, size=n_good) # Histórico alto
df.loc[indices_good, 'is_fraud'] = 0

# 5% dos dados para fraudes
# Valor muito alto em relação à média
n_fraud_high = int(N_SAMPLES * 0.05)
indices_fraud = np.random.choice(df.index.difference(indices_good), size=n_fraud_high, replace=False)
average = df.loc[indices_fraud, 'average_amount']
df.loc[indices_fraud, 'value'] = average * np.random.uniform(5, 10, size=n_fraud_high)
df.loc[indices_fraud, 'is_fraud'] = 1

# 2% dos dados para fraudes de novas contas
mask_new = (df['transaction_count'] == 0) & (df['value'] > 800)
df.loc[mask_new, 'is_fraud'] = 1

print(f"Total de fraudes: {df['is_fraud'].sum()} ({df['is_fraud'].mean():.2%})")

# Treinamento do modelo
FEATURES = ['value', 'transaction_count', 'average_amount', 'is_foreign_country']
X = df[FEATURES]
y = df['is_fraud']

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

print("Treinando o modelo XGBoost...")

model = xgb.XGBClassifier(
    n_estimators=200, # Mais árvores para aprender padrões complexos
    max_depth=5,
    learning_rate=0.05,
    objective='binary:logistic',
    eval_metric='logloss',
    use_label_encoder=False,
    random_state=42
)
model.fit(X_train, y_train)

MODEL_FILE = "model.xgb"
model.save_model(MODEL_FILE)
print(f"Modelo como '{MODEL_FILE}'")