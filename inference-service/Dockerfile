FROM python:3.9-slim

WORKDIR /code

RUN apt-get update && apt-get install -y curl

COPY ./requirements.txt /code/requirements.txt

RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt

COPY ./model.xgb /code/model.xgb

COPY ./app /code/app

EXPOSE 8083

# Inicia a aplicação com o servidor Uvicorn
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8083"]