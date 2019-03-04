#!/usr/bin/env python
# coding=utf-8

from mjtt import Latest_100_Show
import sqlite3
from Utils import Utils
import os


def startSpider():
    last_show = Latest_100_Show()
    page_list = last_show.get_page_list()
    page_infos = []
    for url in page_list:
        try:
            infos = last_show.get_infos(url)
            if infos is not None:
                page_infos.append(infos)
        except Exception, e:
            print e
            continue
    insertData(page_infos)


def insertData(infoList):
    DBName = 'ldb'
    directory_url = '/data/data/com.leox.project.freeshow/databases'
    # directory_url = 'C:\Users\Administrator\Desktop\MoivesSpider-master\mjtt'
    if not os.path.exists(directory_url):
        os.makedirs(directory_url)
    file_url = directory_url + '/' + DBName
    db = sqlite3.connect(file_url, 10)
    conn = db.cursor()

    SelectSql = 'Select * from sqlite_master where type = "table" and name="mjtt";'
    CreateTableSql = '''
        Create Table mjtt (
            'm_id' INTEGER PRIMARY KEY,
            'm_type' varchar(100),
            'm_trans_name' varchar(200),
            'm_name' varchar(100) unique,
            'm_decade' varchar(30),
            'm_country' varchar(30),
            'm_duration' varchar(10),
            'm_director' varchar(50),
            'm_actors' varchar(1000),
            'm_placard' varchar(200),
            'm_ed2k_url' text ,
            'm_magnet_url' text,
            'm_desc' text
        );
    '''

    InsertSql = '''
        Insert or replace into mjtt(m_type, m_trans_name, m_name, m_decade, m_country,m_duration, m_director, m_actors, m_placard, m_ed2k_url,
        m_magnet_url,m_desc)
        values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
    '''
    if not conn.execute(SelectSql).fetchone():
        conn.execute(CreateTableSql)
        db.commit()
        print('====  创建表成功  ====')
    else:
        print('====  创建表失败, 表已经存在  ====')

    count = 1
    infoList.reverse()
    for item in infoList:
        try:
            conn.execute(InsertSql, Utils.dirToList(item))
            db.commit()
            print('插入第 ' + str(count) + ' 条数据成功')
            count = count + 1
        except Exception as e:
            print(e)

    db.commit()
    db.close()
