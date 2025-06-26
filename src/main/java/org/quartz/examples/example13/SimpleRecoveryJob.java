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
 
package org.quartz.examples.example13;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 简单恢复任务实现类
 * 
 * <p>
 * 这是一个用于演示集群故障恢复功能的任务实现。该任务会模拟长时间运行的工作，
 * 并在执行过程中记录执行次数。当集群中的某个实例崩溃时，正在执行的任务
 * 可以被其他实例接管并重新执行。
 * </p>
 * 
 * <p>
 * 关键特性：
 * - 支持故障恢复（通过 requestRecovery() 配置）
 * - 记录任务执行次数
 * - 模拟长时间运行（10秒延迟）
 * - 区分正常执行和恢复执行
 * </p>
 * 
 * @author James House
 */
public class SimpleRecoveryJob implements Job {

  private static Logger       _log  = LoggerFactory.getLogger(SimpleRecoveryJob.class);

  private static final String COUNT = "count";

  /**
   * Quartz 要求提供公共无参构造函数，以便调度器在需要时能够实例化该类
   */
  public SimpleRecoveryJob() {
  }

  /**
   * 任务执行方法
   * 
   * <p>
   * 当与此任务关联的触发器被触发时，调度器会调用此方法。
   * 该方法实现了具体的业务逻辑，包括故障恢复检测、执行计数和模拟工作负载。
   * </p>
   * 
   * @param context 任务执行上下文，包含任务和触发器的详细信息
   * @throws JobExecutionException 如果任务执行过程中发生异常
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {

    // 获取任务的唯一标识
    JobKey jobKey = context.getJobDetail().getKey();

    // 检查任务是否处于恢复模式（即从故障中恢复执行）
    if (context.isRecovering()) {
      _log.info("简单恢复任务: " + jobKey + " 正在从故障中恢复，开始时间: " + new Date());
    } else {
      _log.info("简单恢复任务: " + jobKey + " 正常启动，开始时间: " + new Date());
    }

    // 模拟长时间运行的任务（延迟10秒）
    // 这个延迟使得任务有足够的时间被集群故障转移机制检测和处理
    long delay = 10L * 1000L;
    try {
      Thread.sleep(delay);
    } catch (Exception e) {
      // 忽略中断异常
    }

    // 获取任务数据映射，用于在多次执行间保持状态
    JobDataMap data = context.getJobDetail().getJobDataMap();
    int count;
    if (data.containsKey(COUNT)) {
      // 获取之前的执行次数
      count = data.getInt(COUNT);
    } else {
      // 首次执行，初始化计数器
      count = 0;
    }
    count++;
    // 更新执行次数（注意：普通任务的 JobDataMap 不会自动持久化）
    data.put(COUNT, count);

    _log.info("简单恢复任务: " + jobKey + " 完成时间: " + new Date() + "\n 执行次数: #" + count);

  }

}

