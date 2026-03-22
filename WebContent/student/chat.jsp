<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<jsp:include page="/common/navbar.jsp" />
<jsp:include page="/common/alert_banner.jsp" />
<main class="container">
    <section class="chat-layout">
        <aside class="card online-list" id="online-users"></aside>
        <article class="card chat-window">
            <h2>Chat</h2>
            <div id="messages" class="messages"></div>
            <form id="chat-form" class="row-form">
                <input id="to" name="to" type="text" placeholder="Recipient username" required>
                <input id="message" name="message" type="text" placeholder="Type a message" required>
                <button class="btn btn-primary" type="submit">Send</button>
            </form>
            <p id="chat-status" class="muted"></p>
        </article>
    </section>
</main>
<jsp:include page="/common/footer.jsp" />
<script>
(function () {
    var currentUser = '${sessionScope.username}';
    var form = document.getElementById('chat-form');
    var status = document.getElementById('chat-status');
    var usersNode = document.getElementById('online-users');
    var messagesNode = document.getElementById('messages');
    var toInput = document.getElementById('to');
    var selectedRecipient = '';

    function appendMessage(text, fromSelf) {
        var row = document.createElement('div');
        row.className = fromSelf ? 'msg-self' : 'msg-other';
        row.textContent = text;
        messagesNode.appendChild(row);
        messagesNode.scrollTop = messagesNode.scrollHeight;
    }

    function renderHistory(messages) {
        messagesNode.textContent = '';
        messages.forEach(function (msg) {
            appendMessage(msg.content, !!msg.fromSelf);
        });
    }

    function loadHistory() {
        var to = (toInput.value || '').trim();
        selectedRecipient = to;
        if (!to) {
            messagesNode.textContent = '';
            return;
        }

        fetch('${pageContext.request.contextPath}/chat?action=history&with=' + encodeURIComponent(to))
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (!data || data.status !== 'ok') {
                    return;
                }
                renderHistory(data.messages || []);
            })
            .catch(function () {});
    }

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        var fd = new FormData(form);
        var params = new URLSearchParams();
        for (var pair of fd.entries()) {
            params.append(pair[0], pair[1]);
        }
        fetch('${pageContext.request.contextPath}/chat', { 
            method: 'POST', 
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString() 
        })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.status === 'invalid-request') {
                    status.textContent = data.message || 'Invalid request.';
                    return;
                }
                if (data.status === 'chat-offline') {
                    status.textContent = 'Chat server is currently offline.';
                    return;
                }
                if (data.status === 'invalid-recipient') {
                    status.textContent = 'Recipient username not found.';
                    return;
                }
                status.textContent = 'Message sent.';
                document.getElementById('message').value = '';
                loadHistory();
            })
            .catch(function () { status.textContent = 'Failed to send message.'; });
    });

    toInput.addEventListener('change', loadHistory);

    function pollUsers() {
        fetch('${pageContext.request.contextPath}/status')
            .then(function (res) { return res.json(); })
            .then(function (data) {
                var users = (data.users || []).filter(function (user) {
                    return user !== currentUser;
                });
                usersNode.textContent = '';

                var heading = document.createElement('h3');
                heading.textContent = 'Online';
                usersNode.appendChild(heading);

                if (!users.length) {
                    var empty = document.createElement('p');
                    empty.className = 'empty-state';
                    empty.textContent = 'No users online.';
                    usersNode.appendChild(empty);
                    return;
                }

                if (!selectedRecipient || users.indexOf(selectedRecipient) === -1) {
                    if (users.length > 0) {
                        selectedRecipient = users[0];
                        toInput.value = selectedRecipient;
                        status.textContent = 'Chatting with ' + selectedRecipient + '.';
                        loadHistory();
                    } else {
                        selectedRecipient = '';
                        toInput.value = '';
                    }
                }

                var list = document.createElement('ul');
                users.forEach(function (user) {
                    var item = document.createElement('li');
                    var btn = document.createElement('button');
                    btn.type = 'button';
                    btn.className = 'online-user-btn' + (user === selectedRecipient ? ' active' : '');
                    btn.textContent = user;
                    btn.addEventListener('click', function () {
                        toInput.value = user;
                        selectedRecipient = user;
                        status.textContent = 'Chatting with ' + user + '.';
                        loadHistory();
                        pollUsers();
                    });
                    item.appendChild(btn);
                    list.appendChild(item);
                });
                usersNode.appendChild(list);
            })
            .catch(function () {});
    }

    pollUsers();
    loadHistory();
    setInterval(pollUsers, 5000);
    setInterval(loadHistory, 3000);
})();
</script>
</body>
</html>
