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
 
package org.quartz.examples.example9;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job1 任务监听器实现类
 * 
 * <p>本类实现了 {@code JobListener} 接口，用于监听 SimpleJob1 的执行事件。
 * 当 SimpleJob1 执行完成后，此监听器会自动创建并调度 SimpleJob2，
 * 从而实现任务间的链式执行和依赖关系。</p>
 * 
 * <p>功能特性：</p>
 * <ul>
 * <li>任务生命周期监听：监听任务执行前、执行后、被否决等事件</li>
 * <li>动态任务创建：在监听器回调中创建新的任务实例</li>
 * <li>自动任务调度：将新创建的任务立即提交给调度器执行</li>
 * <li>事件驱动架构：基于事件回调实现任务间的协调</li>
 * </ul>
 * 
 * <p>工作流程：</p>
 * <ol>
 * <li>监听器被注册到调度器并绑定到 SimpleJob1</li>
 * <li>当 SimpleJob1 即将执行时，触发 jobToBeExecuted 方法</li>
 * <li>当 SimpleJob1 执行完成时，触发 jobWasExecuted 方法</li>
 * <li>在 jobWasExecuted 方法中创建 SimpleJob2 并立即调度</li>
 * <li>如果任务执行被否决，触发 jobExecutionVetoed 方法</li>
 * </ol>
 * 
 * <p>关键学习点：</p>
 * <ul>
 * <li>JobListener 接口的三个核心方法实现</li>
 * <li>在监听器中访问调度器实例的方法</li>
 * <li>动态创建任务和触发器的技巧</li>
 * <li>异常处理在监听器中的重要性</li>
 * </ul>
 * 
 * @author wkratzer
 */
public class Job1Listener implements JobListener {

  /** 日志记录器，用于输出监听器的执行信息和调试信息 */
  private static Logger _log = LoggerFactory.getLogger(Job1Listener.class);

  /**
   * 获取监听器名称
   * 
   * <p>返回此监听器的唯一标识名称，用于在调度器中区分不同的监听器实例。
   * 这个名称在注册监听器时会被调度器使用。</p>
   * 
   * @return 监听器名称 "job1_to_job2"，表示从 job1 触发 job2 的监听器
   */
  public String getName() {
    return "job1_to_job2";
  }

  /**
   * 任务即将执行时的回调方法
   * 
   * <p>当被监听的任务即将开始执行时，调度器会调用此方法。
   * 这是任务执行生命周期中的第一个事件，可以在此进行执行前的准备工作。</p>
   * 
   * @param inContext 任务执行上下文，包含任务详情、触发器信息等
   */
  public void jobToBeExecuted(JobExecutionContext inContext) {
    _log.info("Job1Listener 提示: 任务即将开始执行。");
  }

  /**
   * 任务执行被否决时的回调方法
   * 
   * <p>当任务执行被 TriggerListener 否决时，调度器会调用此方法。
   * 这种情况下任务不会实际执行，但监听器仍会收到通知。</p>
   * 
   * @param inContext 任务执行上下文，包含任务详情、触发器信息等
   */
  public void jobExecutionVetoed(JobExecutionContext inContext) {
    _log.info("Job1Listener 提示: 任务执行被否决。");
  }

  /**
   * 任务执行完成后的回调方法
   * 
   * <p>当被监听的任务执行完成后（无论成功还是失败），调度器会调用此方法。
   * 这是实现任务链式执行的关键方法，在此创建并调度 SimpleJob2。</p>
   * 
   * <p>核心功能：</p>
   * <ul>
   * <li>检测 SimpleJob1 的执行完成事件</li>
   * <li>动态创建 SimpleJob2 任务实例</li>
   * <li>创建立即执行的触发器</li>
   * <li>将新任务提交给调度器执行</li>
   * </ul>
   * 
   * @param inContext 任务执行上下文，提供对调度器的访问
   * @param inException 任务执行过程中的异常（如果有的话）
   */
  public void jobWasExecuted(JobExecutionContext inContext, JobExecutionException inException) {
    _log.info("Job1Listener 提示: 任务执行完成。");

    // 创建第二个任务 (SimpleJob2)
    // 使用 JobBuilder 创建任务详情，指定任务类和唯一标识
    JobDetail job2 = newJob(SimpleJob2.class).withIdentity("job2").build();

    // 创建触发器，设置立即启动
    // 这样 SimpleJob2 会在 SimpleJob1 完成后立即执行
    Trigger trigger = newTrigger().withIdentity("job2Trigger").startNow().build();

    try {
      // 将新创建的任务提交给调度器执行
      // 通过执行上下文获取调度器实例，实现动态任务调度
      inContext.getScheduler().scheduleJob(job2, trigger);
    } catch (SchedulerException e) {
      // 如果调度失败，记录警告信息
      _log.warn("无法调度 job2 任务！");
      e.printStackTrace();
    }

  }

}
