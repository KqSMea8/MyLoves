#!/usr/bin/env python
# coding=utf-8

'''
    工具类
@Author monkey
@Date 2017-08-21
'''


class Utils(object):
    '''
    将字典转化为列表
    '''

    @staticmethod
    def dirToList(item):
        itemlist = []
        itemlist.append(item['type'])
        itemlist.append(item['trans_name'])
        itemlist.append(item['name'])
        itemlist.append(item['decade'])
        itemlist.append(item['country'])
        itemlist.append(item['duration'])
        itemlist.append(item['director'])
        itemlist.append(item['actors'])
        itemlist.append(','.join(item['placard']))
        itemlist.append(','.join(item['ed2k_url']))
        itemlist.append(','.join(item['magnet_url']))
        itemlist.append(','.join(item['desc']))
        return itemlist
