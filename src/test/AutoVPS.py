import requests
import time
import json
import os,subprocess
from subprocess import getoutput as go
import sqlite3
import random
import shutil
from csv import reader
try:
    with open(r'C:\View\AutoVPS\Location\Path_Data.txt', 'r') as f_lv:
        read_lv = reader(f_lv)
        lv = list(read_lv)
    path_data = lv[0][0]
except:
        print("Error Path_Data")

try:
    with open(r'C:\View\AutoVPS\Location\Delete_Data.txt', 'r') as f_lv:
        read_lv = reader(f_lv)
        lv_path_delete_data = list(read_lv)
except:
        print("Error Path_Delete_Data")


try:
    with open(r'C:\NameVps.txt', 'r') as f_lv:
        read_lv = reader(f_lv)
        lv = list(read_lv)
    path_vps = lv[0][0]
    print("VPS Name: "+path_vps)
except:
        print("Error NameVps")
try:
    with open(r'C:\View\AutoVPS\Location\Name_Tool.txt', 'r') as f_lv:
        read_lv = reader(f_lv)
        lv = list(read_lv)
    name_tool = lv[0][0]
except:
        print("Error Name_Tool")

try:
    with open(r'C:\View\AutoVPS\Location\Path_Tool.txt', 'r') as f_lv:
        read_lv = reader(f_lv)
        lv = list(read_lv)
    path_tool = lv[0][0]
except:
        print("Error Path_Tool")


def create_connection():
    conn = None
    try:
        conn = sqlite3.connect(path_data)
        print("Connect DB success!")
    except:
        print("Lỗi Connect DB!")

    return conn

def del_data_tool():
    try:
       for i in range(len(lv_path_delete_data)):
            shutil.rmtree(lv_path_delete_data[i][0])
    except:
        print("Lỗi Delete DATA!")
def delete_task(conn):
    """
    Delete a task by task id
    :param conn:  Connection to the SQLite database
    :param id: id of the task
    :return:
    """
    sql = 'Update SettingsTool Set ChangerPass=1 '
    cur = conn.cursor()
    cur.execute(sql)
    conn.commit()
def update(conn):
    """
    Delete a task by task id
    :param conn:  Connection to the SQLite database
    :param id: id of the task
    :return:
    """
    sql = 'Update SettingsTool Set XoaAccDu=0 '
    cur = conn.cursor()
    cur.execute(sql)
    conn.commit()
def check_running(conn):
    try:
        cur = conn.cursor()
        sql = 'Select  XoaAccDu from SettingsTool limit 1 '
        for row in cur.execute(sql):
            pass
        cur.close()
        try:
            return (row)
        except:
            return 0
    except:
        print("EXE ERROR : sql_selec")
connnn=create_connection();
delete_task(connnn)
# vòng while check nhiem vụ
while(True):
    try:
        url = "http://server1.idnetwork.com.vn/vps/checkresetvps?vps="+path_vps;
        payload={}
        headers = {
        'Content-Type': 'application/json',
        'Authorization': '1'
        }

        response = requests.request("GET", url, headers=headers, data=payload)
        data = response.json();
        #print('vpsoptine=')
        print("Vpsoption=" + str(data['vpsreset']))
        if(data['vpsreset']>0 ):
            time.sleep(random.uniform(2,15))
            os.system(r'taskkill /im '+name_tool+r'.exe /f /T')
            time.sleep(2)
            os.system(r'taskkill /im FastExecuteScript.exe /f /T');
            for _ in range(10):
                time.sleep(10)
                ProId=go('wmic process where "ExecutablePath like \'%\FastExecuteScript.exe%\'" get ProcessID')
                if "ProcessId" in ProId:
                    print("Tool is shutting down!")
                else:
                    update(connnn)
                    print("Turned off the tool successfully! Start App...")
                    os.system(r'taskkill /im '+name_tool+r'.exe /f /T')
                    time.sleep(2)
                    if(data['vpsreset']>0 ):
                        del_data_tool()
                        time.sleep(10)
                    os.system(r'Start ""  "'+path_tool+r'"')
                    for _ in range(60):
                        print("Check tool running...")
                        time.sleep(12)
                        ProId=go('wmic process where "ExecutablePath like \'%FastExecuteScript.exe%\'" get ProcessID')
                        if "1" in check_running(connnn):
                            print("Tool is running!")
                            print('Continue check... 120s')
                            time.sleep(120);
                            break
                    break
        else:
            os.system(r'taskkill /im '+name_tool+r'.exe /f /T')
            time.sleep(2)
            ProId=go('wmic process where "ExecutablePath like \'%FastExecuteScript.exe%\'" get ProcessID')
            if "ProcessId" in ProId:
                print("Tool is running!")
                print('Continue check... 120s')
                time.sleep(120);
            else:
                print("Tool not running! Start App...")
                update(connnn)
                os.system(r'taskkill /im '+name_tool+r'.exe /f /T')
                time.sleep(2)
                os.system(r'Start ""  "'+path_tool+r'"')
                for _ in range(12):
                    print("Check tool running...")
                    time.sleep(12)
                    for _ in range(12):
                        print("Check tool running...")
                        time.sleep(12)
                        os.system(r'taskkill /im '+name_tool+r'.exe /f /T')
                        time.sleep(2)
                        ProId=go('wmic process where "ExecutablePath like \'%FastExecuteScript.exe%\'" get ProcessID')
                        if "1" in check_running(connnn):
                            print("Tool is running!")
                            print('Continue check... 120s')
                            time.sleep(120);
                            break
                    break
    except:
        print("Error API")
        time.sleep(120);

