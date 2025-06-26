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
 
package org.quartz.examples.example10;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 插件功能演示示例
 * 
 * <p>本示例演示了 Quartz 调度器中插件系统的强大功能，重点展示了
 * XML 任务初始化插件和历史日志记录插件的使用方法。通过配置文件
 * 驱动的方式，实现了任务定义与代码逻辑的完全分离。</p>
 * 
 * <p>功能特性：</p>
 * <ul>
 * <li>XML 配置驱动：通过外部 XML 文件定义任务和触发器</li>
 * <li>插件系统集成：展示多个插件的协同工作</li>
 * <li>历史记录追踪：自动记录任务执行的详细历史</li>
 * <li>配置文件管理：演示复杂的调度器配置</li>
 * <li>动态任务加载：支持运行时加载任务定义</li>
 * </ul>
 * 
 * <p>执行流程：</p>
 * <ul>
 * <li>加载调度器配置和插件设置</li>
 * <li>XML 插件自动读取任务定义文件</li>
 * <li>历史插件开始监听任务生命周期</li>
 * <li>启动调度器并执行预定义任务</li>
 * <li>监控执行过程并输出统计信息</li>
 * </ul>
 * 
 * <p>关键学习点：</p>
 * <ul>
 * <li>插件配置和使用方法</li>
 * <li>XML 驱动的任务定义方式</li>
 * <li>任务执行历史的记录机制</li>
 * <li>配置与代码分离的最佳实践</li>
 * <li>调度器生命周期管理</li>
 * </ul>
 * 
 * @author James House, Bill Kratzer
 */
public class PlugInExample {

  public void run() throws Exception {
    // 初始化日志记录器
    Logger log = LoggerFactory.getLogger(PlugInExample.class);

    // 创建调度器工厂，会自动加载 quartz.properties 配置文件
    // 配置文件中定义了插件设置，包括 XML 初始化插件和历史日志插件
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = null;
    try {
      // 获取调度器实例，此时插件会被自动加载和初始化
      sched = sf.getScheduler();
    } catch (NoClassDefFoundError e) {
      log.error(" 无法加载类 - 很可能是类路径中缺少 jta.jar。如果 examples/lib 文件夹中没有此文件，" +
                "请添加它以便此示例能够运行。", e);
      return;
    }

    log.info("------- 初始化完成 -----------");

    log.info("------- (不手动调度任务 - 依赖 XML 定义) --");

    log.info("------- 启动调度器 ----------------");

    // 启动调度器
    // XML 初始化插件会在此时读取 quartz_data.xml 文件
    // 并自动创建和调度其中定义的任务和触发器
    sched.start();

    log.info("------- 调度器已启动 -----------------");

    log.info("------- 等待五分钟... -----------");

    // 等待5分钟（300秒）让任务有机会执行
    // 在此期间可以观察到：
    // 1. XML 中定义的任务按照触发器规则执行
    // 2. 历史日志插件记录任务执行事件
    // 3. 控制台输出详细的执行信息
    try {
      Thread.sleep(300L * 1000L);
    } catch (Exception e) {
      // 忽略中断异常
    }

    // 关闭调度器
    log.info("------- 正在关闭 ---------------------");
    // 参数 true 表示等待当前正在执行的任务完成后再关闭
    sched.shutdown(true);
    log.info("------- 关闭完成 -----------------");

    // 获取并输出调度器执行统计信息
    SchedulerMetaData metaData = sched.getMetaData();
    log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务。");
  }

  /**
   * 程序入口方法
   * 
   * <p>演示 Quartz 插件功能的完整流程，包括：</p>
   * <ul>
   * <li>XML 任务初始化插件的配置和使用</li>
   * <li>历史日志记录插件的自动监听</li>
   * <li>配置文件驱动的任务定义方式</li>
   * <li>插件系统与调度器的集成</li>
   * <li>任务执行历史的记录和追踪</li>
   * </ul>
   * 
   * <p>运行此示例可以学习到：</p>
   * <ul>
   * <li>如何通过配置文件设置和使用插件</li>
   * <li>XML 文件定义任务和触发器的语法</li>
   * <li>插件在调度器生命周期中的作用</li>
   * <li>配置与代码分离的最佳实践</li>
   * <li>任务执行监控和统计的实现方式</li>
   * </ul>
   * 
   * @param args 命令行参数（本示例中未使用）
   * @throws Exception 如果示例执行过程中发生异常
   */
  public static void main(String[] args) throws Exception {

    PlugInExample example = new PlugInExample();
    example.run();
  }

}
