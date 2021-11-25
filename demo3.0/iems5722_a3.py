import math
import time

import mysql.connector
from flask import Flask, jsonify, request

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
    return "IEMS5722_A3"


@app.route("/api/a3/get_chatrooms", methods=['GET'])
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


@app.route("/api/a3/get_messages", methods=['GET'])
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


@app.route("/api/a3/send_message", methods=['POST'])
def send_message():
    # get the value
    chatroom_id = request.args.get("chatroom_id")
    user_id = request.args.get("user_id")
    name = request.args.get("name")
    message = request.args.get("message")

    # Connect to the database
    mydb = MyDatabase()
    if message is None or chatroom_id is None or user_id is None or name is None:
        return jsonify(status="ERROR", message="<error message>")
    else:
        mydb.cursor.execute(
            "INSERT INTO messages (chatroom_id, user_id, name, message) VALUE(%s, %s, %s, %s)",
            [chatroom_id, user_id, name, message])
        mydb.conn.commit()
        return jsonify(status="OK")

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True, port=5000)
