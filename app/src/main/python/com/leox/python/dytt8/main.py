#!/usr/bin/env python
# coding=utf-8


import sqlite3

from dytt8Moive import dytt_Lastest
from RequestModel import RequestModel
import requests
from Utils import Utils
import os

'''
    程序主入口
@Author monkey
@Date 2017-08-08
'''

# 截止到2017-08-08, 最新电影一共才有 164 个页面
LASTEST_MOIVE_TOTAL_SUM = 6  # 164

# 请求网络线程总数, 线程不要调太好, 不然会返回很多 400
THREAD_SUM = 5


def startSpider():
    # 实例化对象

    # 获取【最新电影】有多少个页面
    LASTEST_MOIVE_TOTAL_SUM = dytt_Lastest.getMaxsize()
    print('【最新电影】一共  ' + str(LASTEST_MOIVE_TOTAL_SUM) + '  有个页面')
    if LASTEST_MOIVE_TOTAL_SUM > 3:
        LASTEST_MOIVE_TOTAL_SUM = 3
    dyttlastest = dytt_Lastest(LASTEST_MOIVE_TOTAL_SUM)
    floorlist = dyttlastest.getPageUrlList()
    moviePageUrlListResult = []
    infoListResult = []
    for url in floorlist:
        try:
            response = requests.get(url, headers=RequestModel.getHeaders(),
                                    proxies=RequestModel.getProxies(),
                                    timeout=3, verify=False)
            print(' 请求【 ' + url + ' 】的结果： ' + str(response.status_code))

            # 需将电影天堂的页面的编码改为 GBK, 不然会出现乱码的情况
            response.encoding = 'GBK'
            if response.status_code == 200:
                moivePageUrlList = dytt_Lastest.getMoivePageUrlList(response.text)
                host = 'http://www.dytt8.net'
                for item in moivePageUrlList:
                    each = host + item
                    # print(each)
                    moviePageUrlListResult.append(each)
        except Exception as e:
            # print('catsh  Exception ==== ')
            # self.queue.put(url)
            print(e)
    for url in moviePageUrlListResult:
        try:
            response = requests.get(url, headers=RequestModel.getHeaders(),
                                    proxies=RequestModel.getProxies(),
                                    timeout=3, verify=False)
            print(' 请求【 ' + url + ' 】的结果： ' + str(response.status_code))

            # 需将电影天堂的页面的编码改为 GBK, 不然会出现乱码的情况
            response.encoding = 'GBK'

            if response.status_code == 200:
                temp = dytt_Lastest.getMoiveInforms(url, response.text)
                infoListResult.append(temp)
        except Exception as e:
            # self.queue.put(url)
            print(e)

    insertData(infoListResult)


def insertData(infoList):
    DBName = 'ldb'
    directory_url = '/data/data/com.leox.project.freeshow/databases'
    if not os.path.exists(directory_url):
        os.makedirs(directory_url)
    file_url = directory_url + '/' + DBName
    db = sqlite3.connect(file_url, 10)
    conn = db.cursor()

    SelectSql = 'Select * from sqlite_master where type = "table" and name="lastest_moive";'
    CreateTableSql = '''
        Create Table lastest_moive (
            'm_id' INTEGER PRIMARY KEY,
            'm_type' varchar(100),
            'm_trans_name' varchar(200),
            'm_name' varchar(100) unique,
            'm_decade' varchar(30),
            'm_country' varchar(30),
            'm_level' varchar(100),
            'm_language' varchar(30),
            'm_subtitles' varchar(100),
            'm_publish' varchar(30),
            'm_IMDB_score' varchar(50),
            'm_douban_score' varchar(50),
            'm_format' varchar(20),
            'm_resolution' varchar(20),
            'm_size' varchar(10),
            'm_duration' varchar(10),
            'm_director' varchar(50),
            'm_actors' varchar(1000),
            'm_placard' varchar(200),
            'm_screenshot' varchar(200),
            'm_ftp_url' varchar(200) unique,
            'm_dytt8_url' varchar(200),
            'm_desc' varchar(200)
        );
    '''

    InsertSql = '''
        Insert or replace into lastest_moive(m_type, m_trans_name, m_name, m_decade, m_country, m_level, m_language, m_subtitles, m_publish, m_IMDB_score, 
        m_douban_score, m_format, m_resolution, m_size, m_duration, m_director, m_actors, m_placard, m_screenshot, m_ftp_url,
        m_dytt8_url,m_desc)
        values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?);
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


if __name__ == '__main__':
    startSpider()
