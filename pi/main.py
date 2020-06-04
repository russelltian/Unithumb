from gpiozero import LED
from time import sleep
import socket


if __name__ == "__main__":


    #host = socket.gethostbyname(socket.gethostname())
    host = '192.168.0.29'
    print("host is " + host)

    port = 8000
    BUFF_SIZE = 32


    left = LED(16)
    right = LED(12)

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    #s.setblocking(0)
    print("socket created")

    try:
        s.bind((host,port))
    except socket.error as e:
        print("binding failed")
        print(e)
        sys.exit()

    print("socket ready")
    s.listen(3)

    conn, addr = s.accept()
    conn.setblocking(0)
    print("connected by " + addr[0] +" : " + str(addr[1]))

    while True:
        msg = ""
        try:
            data = conn.recv(BUFF_SIZE)
            msg = data.decode("utf-8") 
        except socket.error, e:
            #ignore
            pass
        

        
        
        if msg == "left":
            left.on()
            right.off()
            print(msg)
        elif msg == "right":
            right.on()
            left.off()
            print(msg)
        elif msg == "stop":
            left.off()
            right.off()

        # left.on()
        # left.off()
        
        # sleep(1)
        # right.on()
        # right.off()
        # sleep(1)
