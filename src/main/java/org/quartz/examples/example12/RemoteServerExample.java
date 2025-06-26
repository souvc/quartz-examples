/* 
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */
 
package org.quartz.examples.example12;

import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RMI 远程调度服务器示例
 * 
 * 本类演示如何创建一个 Quartz 调度服务器，该服务器通过 RMI 接受来自远程客户端的任务调度请求。
 * 服务器本身不创建任何任务，而是等待客户端通过 RMI 连接来提交任务。
 * 
 * 主要功能：
 * - 启动 Quartz 调度器并暴露 RMI 接口
 * - 等待远程客户端连接和任务提交
 * - 执行由客户端调度的任务
 * - 提供任务执行统计信息
 * 
 * @author Bill Kratzer
 */
public class RemoteServerExample {

  /**
   * 运行 RMI 服务器示例
   * 
   * 此方法启动一个 Quartz 调度器服务器，该服务器通过 RMI 暴露调度接口，
   * 允许远程客户端连接并提交任务。服务器将运行 10 分钟来演示远程任务调度。
   * 
   * @throws Exception 如果调度器启动或运行过程中发生错误
   * @author James House, Bill Kratzer
   */
  public void run() throws Exception {
    Logger log = LoggerFactory.getLogger(RemoteServerExample.class);

    // 首先获取调度器的引用
    // StdSchedulerFactory 会读取 server.properties 配置文件
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- 初始化完成 -----------");

    log.info("------- (不调度任何任务 - 依赖远程客户端来调度任务) --");

    log.info("------- 启动调度器 ----------------");

    // 启动调度器，此时调度器开始监听 RMI 请求
    // 配置文件中的 rmi.export=true 使调度器可被远程访问
    sched.start();

    log.info("------- 调度器已启动 -----------------");

    log.info("------- 等待十分钟... ------------");

    // 等待 10 分钟，给远程客户端提交的任务运行的机会
    // 在实际应用中，服务器通常会持续运行而不是定时退出
    try {
      Thread.sleep(600L * 1000L); // 600 秒 = 10 分钟
    } catch (Exception e) {
      // 忽略中断异常
    }

    // 关闭调度器
    log.info("------- 正在关闭 ---------------------");
    // shutdown(true) 表示等待当前正在执行的任务完成后再关闭
    sched.shutdown(true);
    log.info("------- 关闭完成 -----------------");

    // 获取并输出调度器的统计信息
    SchedulerMetaData metaData = sched.getMetaData();
    log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务。");
  }

  public static void main(String[] args) throws Exception {

    RemoteServerExample example = new RemoteServerExample();
    example.run();
  }

}
