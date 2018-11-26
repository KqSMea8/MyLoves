#!/usr/bin/env python
# coding=utf-8

import requests
import lxml.etree as etree
from RequestModel import RequestModel


class Latest_100_Show(object):
    start_url = 'https://www.meijutt.com/new100.html'
    base_url = 'https://www.meijutt.com'

    def __init__(self):
        self.max_size = 100

    # <ul class="top-list  fn-clear">
    # <li>
    # <div class="lasted-num fn-left"><i>1</i></div>
    # <h5><a href="/content/meiju23714.html" title="芝加哥烈焰第七季" target="_blank">芝加哥烈焰第七季</a></h5>
    # <span class="state1 new100state1"><font color="#06B9D1">第7集
    # <span class="sub"><em class="subsheng">无字</em></font> </span>
    # <span class="mjjq">罪案,剧情</span>
    # <span class="mjtv">NBC</span>
    # <div class="lasted-time new100time fn-right"><font color="#E12160">2018-11-8</font></div></li>
    def get_page_list(self):
        page_list = []
        response = requests.get(self.start_url, headers=RequestModel.getHeaders(),
                                proxies=RequestModel.getProxies(), timeout=3, verify=False)
        if response.status_code == 200:
            response.encoding = 'gb2312'
            selector = etree.HTML(response.text)
            content = selector.xpath("//ul[@class='top-list  fn-clear']/li/h5/a[@title]/@href")
            return content
        else:
            print("request page list error")
        return page_list

    # <meta name="description" content="《芝加哥烈焰第七季》剧情介绍:　　NBC续订了芝加哥背景的三部剧《凤城火情》(续订第7季)、《芝加哥警署》(续订第6季)、《芝加哥医院》(续订第4季)。" />

    # <div class="o_r_contact"><ul>
    # <li><font color="#06B9D1">至第7集</font> / <font color="#FF7E00">共20集</font> <font color="#FF7E00"><span>周四</span>更新  </font></li>
    # <li><em>原名：</em>Chicago Fire Season 7</li>
    # <li><em>别名：</em>风城烈火/风城烈焰</li>
    # <li ><em>编剧：</em><span>迈克尔·布拉德特,德里克·哈斯</span><span class="more-text">更多&gt;&gt;</span></li>
    # <li ><em>导演：</em><span></span><span class="more-text">更多&gt;&gt;</span></li>
    # <li ><em>主演：</em><span>杰西·斯宾塞,泰勒·金尼,卡拉·吉尔默,大卫·艾根伯格,尤里·沙尔达罗夫,乔·米诺索,克里斯蒂安·斯托特,Miranda·Rae·Mayo,伊默恩·沃克,史蒂文·博耶,安妮·伊隆泽,莫里斯·约翰逊</span><span class="more-text">更多&gt;&gt;</span></li>
    # <li><em>首播日期：</em>2018-09-27</li>
    # <li><em>翻译：</em><span >人人字幕组&nbsp&nbsp</span></li><li>
    # <em>小分类：</em><a href="/drama/zuian.html" target="_self">罪案</a><a href="/drama/juqing.html" class="last-unit" target="_self">剧情</a> </li>
    # <li><label><em>地区：</em>美国</label></li>
    # <li><label><em>电视台：</em>NBC</label><span><em>单集片长：</em>40分钟</span></li>
    # <li><label><em>时间：</em>2018/11/8 12:24:50</label><span><em>类型：</em>犯罪/历史</span></li></ul></div>
    def get_infos(self, url):
        contentDir = {
            'type': '',
            'trans_name': '',
            'name': '',
            'decade': '',
            'country': '',
            'duration': '',
            'director': '',
            'actors': '',
            'placard': '',
            'ed2k_url': [],
            'magnet_url': [],
            'desc': ''
        }
        response = requests.get(self.base_url + url, headers=RequestModel.getHeaders(),
                                proxies=RequestModel.getProxies(), timeout=3, verify=False)
        if response.status_code == 200:
            response.encoding = 'gb2312'
            selector = etree.HTML(response.text)
            contentDir['desc'] = selector.xpath("//meta[@name='description']/@content")
            contentDir['placard'] = selector.xpath(
                "//div[@class='info-box']/div[@class='o_list']/div[@class='o_big_img_bg_b']/img/@src")
            content = selector.xpath("//div[@class='o_r_contact']/ul/li/em/text()"
                                     "|//div[@class='o_r_contact']/ul/li/label/em/text()"
                                     "|//div[@class='o_r_contact']/ul/li/label/text()"
                                     "| //div[@class='o_r_contact']/ul/li/span/em/text()"
                                     "| //div[@class='o_r_contact']/ul/li/span/text()"
                                     "|//div[@class='o_r_contact']/ul/li/text()")
            i = 0
            while i < content.__len__():
                if content[i] == u'原名：':
                    contentDir['name'] = content[i + 1]
                elif content[i] == u'别名：':
                    if content[i + 1] != u'编剧：':
                        contentDir['trans_name'] = content[i + 1]
                elif content[i] == u'导演：':
                    if content[i + 1] == u'\u66f4\u591a>>':
                        contentDir['director'] = ""
                    else:
                        contentDir['director'] = content[i + 1]
                        i += 1
                elif content[i] == u'类型：':
                    contentDir['tyoe'] = content[i + 1]
                elif content[i] == u'首播日期：':
                    contentDir['decade'] = content[i + 1]
                elif content[i] == u'地区：':
                    contentDir['country'] = content[i + 1]
                elif content[i] == u'单集片长：':
                    contentDir['duration'] = content[i + 1]
                elif content[i] == u'主演：':
                    if content[i + 1] == u'\u66f4\u591a>>':
                        contentDir['actors'] = ""
                    else:
                        contentDir['actors'] = content[i + 1]
                        i += 1
                else:
                    i -= 1
                i += 2
            urls = selector.xpath(
                "//div[@class='tabs-list current-tab']/div[@class='down_list']/ul/li/p/strong/a/@href|//div[@class='tabs-list']/div[@class='down_list']/ul/li/p/strong/a/@href")
            j = 0
            is_ed2k_started = False
            while j < urls.__len__():
                if urls[j].startswith(u'magnet:?'):
                    if is_ed2k_started:
                        if contentDir['magnet_url'].__len__() < contentDir['ed2k_url'].__len__():
                            contentDir['magnet_url'].append(urls[j])
                else:
                    if not is_ed2k_started:
                        is_ed2k_started = True
                    contentDir['ed2k_url'].append(urls[j])
                j += 1
            print contentDir
            return contentDir
        else:
            print response.status_code
        return None


