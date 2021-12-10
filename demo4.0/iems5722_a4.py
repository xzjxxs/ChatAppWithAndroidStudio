import math
import time

import mysql.connector
from flask import Flask, jsonify, request
from task import notify_everyone

app = Flask(__name__)


# Create a database class for readability and resuability
class MyDatabase:
    conn = None
    cursor = None

    def __init__(self):
        self.connect()
        return

    def connect(self):
        self.conn = mysql.connector.connect(
            host='localhost',
            user='xuzijun',
            password='1155161089',
            database='iems5722',
        )
        # Create a cursor that return rows as dictionaries
        self.cursor = self.conn.cursor(dictionary=True)
        return


@app.route("/")
def index():
    return "IEMS5722_A4"


@app.route("/api/a4/get_chatrooms", methods=['GET'])
def get_chatrooms():
    # Connect to the database
    mydb = MyDatabase()
    # Execute the query
    mydb.cursor.execute("SELECT * FROM chatrooms")
    # Retrieve all the results
    # "results" is a list of rows, each row is a dictionary
    chatrooms = mydb.cursor.fetchall()
    # Format and return the data
    if chatrooms is None:
        return jsonify(status="ERROR", message="<error message>")
    else:
        return jsonify(status="OK", data=chatrooms)


@app.route("/api/a4/get_messages", methods=['GET'])
def get_messages():
    # get the value
    curr_page = request.args.get("page")
    chatroom_id = request.args.get("chatroom_id")
    msg_id = (int(curr_page) - 1) * 5

    # Connect to the database
    mydb = MyDatabase()
    # Execute the query
    mydb.cursor.execute("SELECT * FROM messages WHERE chatroom_id =" + chatroom_id + " ORDER BY id DESC")
    # Retrieve all the results
    # "results" is a list of rows, each row is a dictionary
    messages = mydb.cursor.fetchall()
    # calculate the total number of page
    total_page = math.ceil(len(messages) / 5)
    # Format and return the data
    if messages is None or int(curr_page) > total_page:
        return jsonify(status="ERROR", message="<error message>")
    else:
        # format the return time info
        for m in messages:
            print(m['message_time'])
            m["message_time"] = m["message_time"].strftime("%Y-%m-%d %H:%M")

        return jsonify(
            {
                'data':
                    {
                        'current_page': int(curr_page),
                        'messages': messages[msg_id:msg_id + 5],
                        'total_pages': total_page
                    },
                'status': "OK"
            })


@app.route("/api/a4/send_message", methods=['POST'])
def send_message():
    # get the value
    chatroom_id = request.form.get("chatroom_id")
    user_id = request.form.get("user_id")
    name = request.form.get("name")
    message = request.form.get("message")

    # Connect to the database
    mydb = MyDatabase()
    if message is None or chatroom_id is None or user_id is None or name is None:
        return jsonify(status="ERROR", message="<error message>")
    else:
        mydb.cursor.execute(
            "INSERT INTO messages (chatroom_id, user_id, name, message) VALUES (%s, %s, %s, %s)",
            [chatroom_id, user_id, name, message])
        mydb.conn.commit()
        print("Send to message successfully")

        # get the chatroom name
        mydb.cursor.execute("SELECT name FROM chatrooms WHERE id=%s" % chatroom_id)
        chatroom_name_json = mydb.cursor.fetchone()
        chatroom_name = chatroom_name_json['name']

        # push the message to everyone in the chatroom
        mydb.cursor.execute("SELECT * FROM push_tokens")
        token_json = mydb.cursor.fetchone()
        # print("send from："+ user_id)
        while token_json is not None:
            # print(user_id)
            if str(token_json['user_id']) != user_id:
                token = token_json['token']
                notify_everyone.delay(chatroom_name, chatroom_id, name, message, token)
            token_json = mydb.cursor.fetchone()
        print("Push messages successfully")
        return jsonify(status="OK")


@app.route("/api/a4/submit_push_token", methods=['POST'])
def submit_push_token():
    # get the value
    user_id = request.form.get("user_id")
    token = request.form.get("token")

    # Connect to the database
    mydb = MyDatabase()
    if token is None or user_id is None:
        return jsonify(status="error", message="Missing Info")
    else:
        mydb.cursor.execute(
            "INSERT INTO push_tokens (user_id, token) VALUES (%s, %s)", [user_id, token])
        mydb.conn.commit()
        return jsonify(status="OK")


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True, port=5000)
