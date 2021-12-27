import math
import os
import time

import mysql.connector
import requests
import werkzeug
from flask import Flask, jsonify, request, render_template, redirect, url_for, send_file
from flask_socketio import SocketIO, emit

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
            buffered=True
        )
        # Create a cursor that return rows as dictionaries
        self.cursor = self.conn.cursor(dictionary=True, buffered=True)
        return


@app.route("/")
def index():
    return "IEMS5722"


@app.route("/api/a4/get_chatrooms/<userId>", methods=['GET'])
def get_chatrooms(userId):
    # Connect to the database
    mydb = MyDatabase()
    # Execute the query
    mydb.cursor.execute("SELECT * FROM chatrooms WHERE user_id =%s OR friend_id=%s OR user_id ='public'",
                        [userId, userId])
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
        # emit('send', 'send it')
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


@app.route('/api/a4/login', methods=['POST'])
def login():
    # get the value
    userId = request.form.get("userId")
    password = request.form.get("password")
    username = request.form.get("username")

    # Connect to the database
    mydb = MyDatabase()
    # check if has such account
    mydb.cursor.execute("SELECT * FROM users WHERE user_id =%s",[userId])
    if mydb.cursor.fetchone() is None:
        return "0"
    mydb.cursor.execute("SELECT * FROM users WHERE user_id =%s AND password =%s AND username=%s",
                        [userId, password, username])
    if mydb.cursor.fetchone() is not None:
        return jsonify(status="OK")
    else:
        print("log fail")
        return '1'


@app.route('/api/a4/register', methods=['POST'])
def register():
    # get the value
    userId = request.form.get("userId")
    password = request.form.get("password")
    username = request.form.get("username")

    # Connect to the database
    mydb = MyDatabase()
    mydb.cursor.execute("SELECT * FROM users WHERE user_id =%s" % userId)
    if mydb.cursor.fetchone() is not None:
        print("register fail")
        return "0"
    else:
        mydb.cursor.execute(
            "INSERT INTO users (user_id, password, username) VALUES (%s, %s, %s)", [userId, password, username])
        mydb.conn.commit()
        print("register successful")
        return jsonify(status="OK")


@app.route('/api/a4/addfriend', methods=['POST'])
def addFriend():
    # get the value
    userId = request.form.get("userId")
    friendName = request.form.get("friendName")
    friendId = request.form.get("friendId")
    userName = request.form.get("userName")

    # Connect to the database
    mydb = MyDatabase()
    # check if friend is user
    mydb.cursor.execute("SELECT * FROM users WHERE user_id =%s" % friendId)
    if mydb.cursor.fetchone() is None:
        print("no this user")
        return "no this friend"

    # check if friend is exist
    mydb.cursor.execute("SELECT * FROM friends WHERE addfriendsid=%s AND user_id=%s", [friendId, userId])
    if mydb.cursor.fetchone() is not None:
        print("friend exist")
        return "friend exist"

    mydb.cursor.execute(
        "INSERT INTO friends (addfriendsid, addfriendsname, user_id) VALUES (%s, %s, %s)",
        [friendId, friendName, userId])
    mydb.conn.commit()
    newChatroomName = userName+'-'+friendName
    mydb.cursor.execute(
        "INSERT INTO chatrooms (name, user_id, friend_id) VALUES (%s, %s, %s)", [newChatroomName, userId, friendId])
    mydb.conn.commit()
    print("add friend successful")
    return jsonify(status="OK")


@app.route('/api/a4/myUpload/<userId>', methods=['GET', 'POST'])
def handle_request(userId):
    files_ids = list(request.files)
    print("\nNumber of Received Images : ", len(files_ids))
    image_num = 1
    for file_id in files_ids:
        print("\nSaving Image ", str(image_num), "/", len(files_ids))
        imagefile = request.files[file_id]
        filename = werkzeug.utils.secure_filename(imagefile.filename)
        print("Image Filename : " + imagefile.filename)
        timestr = time.strftime("%Y%m%d-%H%M%S")
        if not os.path.exists(userId):
            os.makedirs(userId, mode=0o777)
        imagefile.save(os.path.join(userId + '/' + timestr + '_' + filename))
        image_num = image_num + 1
    print("\n")
    return "Image(s) Uploaded Successfully."


# handle file upload function
@app.route('/api/a4/myUpload/myfile', methods=['GET', 'POST'])
def login_file():
    error = None
    if request.method == 'POST':
        userId = request.form['user_id']
        password = request.form['password']
        # Connect to the database
        mydb = MyDatabase()
        mydb.cursor.execute("SELECT * FROM users WHERE user_id=%s AND password=%s", [userId, password])
        if mydb.cursor.fetchone() is not None:
            return redirect(url_for('manage_file', userId=userId))
        else:
            error = 'Invalid userID/password!'
            return render_template('myfile.html', error=error)
        # user = User.query.filter(User.accountNumber == form.accountNumber.data,
        #                          User.password == form.password.data).first()
    return render_template('myfile.html', error=error)


@app.route('/api/a4/manage/<userId>')
def manage_file(userId):
    files_list = os.listdir(userId)
    return render_template('manage.html', files_list=files_list, userId=userId)


@app.route('/api/a4/open/<userId>/<filename>')
def open_file(filename, userId):
    # download file
    return send_file(userId + '/' + filename, as_attachment=True)


@app.route('/api/a4/delete/<userId>/<filename>')
def delete_file(filename, userId):
    # delete file
    os.remove(userId + '/' + filename)
    return redirect(url_for('manage_file', userId=userId))


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True, port=5000)
