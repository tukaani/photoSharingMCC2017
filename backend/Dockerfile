FROM python:3.5.2-slim

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY requirements.txt /usr/src/app

RUN pip install -r requirements.txt

COPY . /usr/src/app

ENTRYPOINT ["python"]

CMD ["main.py"]