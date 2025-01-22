import socket
import threading

# 클라이언트 1 (스마트폰)과의 연결을 관리하는 함수
def handle_client_1(client_socket, client_address, unity_socket):
    try:
        while True:
            data = client_socket.recv(1024).decode()
            if not data:
                break
            print(f"클라이언트 1 (스마트폰): {data}")
            # 클라이언트 1에서 받은 데이터를 클라이언트 2 (Unity)로 전달
            unity_socket.send(data.encode())
    finally:
        client_socket.close()

# 클라이언트 2 (Unity)와의 연결을 관리하는 함수
def handle_client_2(client_socket, client_address):
    try:
        while True:
            data = client_socket.recv(1024).decode()
            if not data:
                break
            print(f"클라이언트 2 (Unity): {data}")
    finally:
        client_socket.close()

# 서버 소켓 설정
def start_server():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(('0.0.0.0', 12346))  # 서버 IP와 포트
    server_socket.listen(2)  # 최대 2개의 클라이언트 연결을 대기

    print("서버가 시작되었습니다. 클라이언트 1(스마트폰)과 클라이언트 2(Unity) 연결을 기다립니다...")

    # 클라이언트 1 (스마트폰) 연결 대기
    client_2_socket, client_2_address = server_socket.accept()
    print(f"클라이언트 2 (유니티) 연결됨: {client_2_address}")

    # 클라이언트 2 (Unity) 연결 대기
    client_1_socket, client_1_address = server_socket.accept()
    print(f"클라이언트 1 (스마트폰) 연결됨: {client_2_address}")

    # 클라이언트 1과 클라이언트 2의 연결을 각각 처리하는 스레드 시작
    threading.Thread(target=handle_client_1, args=(client_1_socket, client_1_address, client_2_socket)).start()
    threading.Thread(target=handle_client_2, args=(client_2_socket, client_2_address)).start()

if __name__ == "__main__":
    start_server()
