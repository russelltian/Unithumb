# Servo Control
import time
import wiringpi
import socket
import math
 
# use 'GPIO naming'
wiringpi.wiringPiSetupGpio()
 
# set #18 to be a PWM output
wiringpi.pinMode(12, wiringpi.GPIO.PWM_OUTPUT)
 
# set the PWM mode to milliseconds stype
wiringpi.pwmSetMode(wiringpi.GPIO.PWM_MODE_MS)
 
# divide down clock
wiringpi.pwmSetClock(192)
wiringpi.pwmSetRange(2000)
 
delay_period = 0.01


host = 'localhost'
print("host is " + host)

port = 12345
BUFF_SIZE = 32

imu_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
imu_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

try:
    imu_socket.bind((host,port))
except socket.error as e:
    print("binding failed")
    print(e)
    sys.exit()

print("imu_socket ready")
imu_socket.listen(3)

i_conn, i_addr = imu_socket.accept()
i_conn.setblocking(0)
print("connected by " + i_addr[0] +" : " + str(i_addr[1]))

#host for phone
host = '192.168.43.107'
print("host is " + host)
port = 8001
BUFF_SIZE = 64
phone_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
phone_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
try:
    phone_socket.bind((host,port))
except socket.error as e:
    print("binding failed")
    print(e)
    sys.exit()

print("phone_socket ready")
phone_socket.listen(3)

p_conn, p_addr = phone_socket.accept()
p_conn.setblocking(0)
print("connected by " + p_addr[0] +" : " + str(p_addr[1]))

print("calibrating")
for i in range(50000):
    msg = ""
    try:
        data = i_conn.recv(BUFF_SIZE)
        msg = data.decode("utf-8")
    except Exception as e:
        #ignore
        pass
    if msg != "":
        
        try:
            yaw = float(msg)
        except Exception as e:
            #print(e)
            pass
print("Done Calibrating, offset is ",yaw)
offset = yaw
bearing = 0
 
while True:
    #phone data
    msg = ""
    try:
        data = p_conn.recv(BUFF_SIZE)
        msg = data.decode("utf-8")
    except Exception as e:
        #ignore
        pass
    if msg != "":
        try:
            bearing = float(msg)
            #print("bearing is",bearing)
        except Exception as e:
            print(e,"bearing failed to decode")
            pass
    
    #imu data
    msg = ""
    try:
        data = i_conn.recv(BUFF_SIZE)
        msg = data.decode("utf-8")
    except Exception as e:
        #ignore
        pass
    if msg != "":
        
        try:
            yaw = float(msg)
            angle = yaw-offset
            if angle>180:
                angle-=360
            elif angle<-180:
                angle+=360
            #todo:boundry check
            out = angle-bearing
            if out>180:
                out-=360
            elif out<-180:
                out+=360
            pulse = int(((out+90)*250)/180)
            wiringpi.pwmWrite(18, pulse)
            time.sleep(delay_period)
            print(offset,out,pulse,bearing)
        except Exception as e:
            print(e)
            pass
        
