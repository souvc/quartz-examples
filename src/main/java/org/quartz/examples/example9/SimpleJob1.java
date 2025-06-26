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

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单任务1实现类
 * 
 * <p>这是一个简单的任务实现，作为监听器示例中的第一个任务。
 * 此任务会被 Job1Listener 监听，当任务执行完成后，监听器会自动
 * 创建并调度 SimpleJob2，从而演示任务间的链式执行机制。</p>
 * 
 * <p>功能特点：</p>
 * <ul>
 * <li>实现 Job 接口：提供标准的任务执行方法</li>
 * <li>输出执行信息：显示任务名称和执行时间</li>
 * <li>被监听器监听：作为触发链式执行的起始任务</li>
 * <li>适用于演示：简单明了地展示监听器机制</li>
 * </ul>
 * 
 * <p>在监听器示例中的作用：</p>
 * <ul>
 * <li>作为被监听的目标任务</li>
 * <li>触发监听器的事件回调</li>
 * <li>启动任务链式执行流程</li>
 * <li>验证监听器的工作机制</li>
 * </ul>
 * 
 * @author Bill Kratzer
 */
public class SimpleJob1 implements Job {

    /** 日志记录器，用于输出任务执行信息 */
    private static Logger _log = LoggerFactory.getLogger(SimpleJob1.class);

    /**
     * 空构造函数
     * 
     * <p>为 Quartz 调度器通过反射机制创建任务实例提供支持。
     * 调度器在执行任务时会使用无参构造函数来实例化任务对象。</p>
     */
    public SimpleJob1() {
    }

    /**
     * 任务执行方法
     * 
     * <p>当与此任务关联的 {@code Trigger} 触发时，
     * {@code Scheduler} 会调用此方法来执行任务逻辑。</p>
     * 
     * <p>在监听器示例中的作用：</p>
     * <ul>
     * <li>输出任务执行信息，便于观察任务执行过程</li>
     * <li>作为被监听的目标，触发 Job1Listener 的事件回调</li>
     * <li>启动任务链式执行的第一步</li>
     * <li>验证监听器机制的正确性</li>
     * </ul>
     * 
     * @param context 任务执行上下文，包含任务详情、触发器信息等
     * @throws JobExecutionException 如果任务执行过程中发生异常
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        // 获取任务的唯一标识键
        JobKey jobKey = context.getJobDetail().getKey();
        // 输出任务执行信息，包括任务名称和执行时间
        _log.info("SimpleJob1 提示: " + jobKey + " 正在执行，时间: " + new Date());
    }

}
