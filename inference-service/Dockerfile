FROM python:3.9-slim
WORKDIR /code

# Atualiza o apt-get e instala dependências do sistema
RUN apt-get update && apt-get install -y curl

COPY ./requirements.txt /code/requirements.txt
RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt

# Copia o modelo treinado
COPY ./xgboost_model.json /code/xgboost_model.json

# Copia o arquivo .proto
COPY ./proto /code/proto

# Copia o código da aplicação
COPY ./app /code/app

# Gera o código gRPC dentro da imagem
RUN mkdir -p /code/app/grpc_generated && \
    python3 -m grpc_tools.protoc -I./proto --python_out=./app/grpc_generated --grpc_python_out=./app/grpc_generated ./proto/fraud_detection.proto

# Expõe a porta gRPC
EXPOSE 50051

# Inicia o servidor gRPC
CMD ["python3", "app/server_grpc.py"]