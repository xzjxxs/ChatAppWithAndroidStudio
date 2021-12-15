# Provides APIs to send WebSocket message
# Handles WebSocket connections
import requests
from flask import Flask, jsonify, request
from flask_socketio import SocketIO, emit, join_room, leave_room

app = Flask(__name__)
app.config['SECRET_KEY'] = 'iems5722'
socketio = SocketIO(app)


# Invoked whenever a client is connected
@socketio.on('connect')
def connect_handler():
    print('Client connected')


# Invoked whenever a client is disconnected
@socketio.on('disconnect')
def disconnect_handler():
    print("Client disconnected")


# Submit a WebSocket message to be broadcasted to a specific room
# @app.route("/api/socketio/broadcast_room", methods=["POST"])
@socketio.on('send')
def broadcast_room(data):
    # chatroom_id = request.form['chatroom_id']
    # message = request.form['message']
    print(data)
    # print(message)
    chatroom_id = data

    # payload = {"chatroom_id": chatroom_id}
    if chatroom_id is None:
        return jsonify(status="ERROR", message="<error message>")
    else:
        socketio.emit("NewMessage", chatroom_id, room=chatroom_id, include_self=False)
        return jsonify(status="OK")


@socketio.on('join')
def on_join(data):
    # join the room with chatroomId
    join_room(data)
    print("A user join room " + data)


@socketio.on('leave')
def on_leave(data):
    # leave the room with chatroomId
    leave_room(data)
    print("A user leave room " + data)


@socketio.on('text')
def update_handler(msg):
    print(msg)
    emit('update', msg, broadcast=True)


if __name__ == '__main__':
    socketio.run(app, host='0.0.0.0', debug=True, port=8001)
