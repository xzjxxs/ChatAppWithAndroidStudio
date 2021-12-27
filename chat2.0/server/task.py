import requests
from celery import Celery
from flask import Flask


# Create a new Celery object
def make_celery(app):
    celery = Celery(
        app.import_name,
        backend=app.config['CELERY_RESULT_BACKEND'],
        broker=app.config['CELERY_BROKER_URL']
    )
    celery.conf.update(app.config)

    class ContextTask(celery.Task):
        def __Call__(self, *args, **kwargs):
            with app.app_context():
                return self.run(*args, **kwargs)

    celery.Task = ContextTask
    return celery


# Create a flask app to initialize the Celery object
app = Flask(__name__)
app.config.update(
    CELERY_BROKER_URL='amqp://guest@localhost//',
    CELERY_RESULT_BACKEND='redis://localhost:6379'
)
celery = make_celery(app)


@celery.task()
def notify_everyone(chatroom_name, chatroom_id, name, message, token):
    # server key
    api_key = 'AAAA5Z-nIPo:APA91bEohamsl-fhutkWY4_F56_wFAtg0z1J5uHvHLAGM1zPGszCwJzZAj7FIQ1r-32DFM3BHa7qjSI6203kN2UZIai8VJMcez9sdUx3107D0_w8gEgJKzvCOZ3ZdAVgc7MgXlUetV4m'
    url = 'https://fcm.googleapis.com/fcm/send'

    headers = {
        'Authorization': 'key=' + api_key,
        'Content-Type': 'application/json'
    }

    payload = {"to": token, "data": {
        "title": chatroom_name,
        "tag": chatroom_id,
        "body": name + ": " + message
    }}

    response = requests.post(url, headers=headers, json=payload)
    if response.status_code == 200:
        print("Send to FCM successfully")

    # # call the api in flask_socketio
    # print(chatroom_id)
    # broadcast_url = "http://18.116.63.13:8001/api/socketio/broadcast_room"
    # payload = {'chatroom_id': chatroom_id, 'message': message}
    # r = requests.post(broadcast_url, data=payload)
    # if r.status_code == 200:
    #     print("Broadcast successfully")
