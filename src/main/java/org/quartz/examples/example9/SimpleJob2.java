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
 * 简单任务2实现类
 * 
 * <p>这是一个简单的任务实现，作为监听器示例中的第二个任务。
 * 此任务由 Job1Listener 在 SimpleJob1 执行完成后动态创建和调度，
 * 演示了基于事件驱动的任务链式执行机制。</p>
 * 
 * <p>功能特点：</p>
 * <ul>
 * <li>实现 Job 接口：提供标准的任务执行方法</li>
 * <li>输出执行信息：显示任务名称和执行时间</li>
 * <li>动态创建调度：由监听器在运行时创建和调度</li>
 * <li>链式执行验证：证明任务间依赖关系的实现</li>
 * </ul>
 * 
 * <p>在监听器示例中的作用：</p>
 * <ul>
 * <li>作为链式执行的第二个环节</li>
 * <li>验证监听器动态调度功能</li>
 * <li>展示事件驱动的任务协调机制</li>
 * <li>完成任务依赖关系的演示</li>
 * </ul>
 * 
 * @author Bill Kratzer
 */
public class SimpleJob2 implements Job {

    /** 日志记录器，用于输出任务执行信息 */
    private static Logger _log = LoggerFactory.getLogger(SimpleJob2.class);

    /**
     * 空构造函数
     * 
     * <p>为 Quartz 调度器通过反射机制创建任务实例提供支持。
     * 调度器在执行任务时会使用无参构造函数来实例化任务对象。</p>
     */
    public SimpleJob2() {
    }

    /**
     * 任务执行方法
     * 
     * <p>当与此任务关联的 {@code Trigger} 触发时，
     * {@code Scheduler} 会调用此方法来执行任务逻辑。</p>
     * 
     * <p>在监听器示例中的作用：</p>
     * <ul>
     * <li>输出任务执行信息，便于观察链式执行效果</li>
     * <li>作为监听器动态调度的目标任务</li>
     * <li>验证事件驱动任务创建的成功性</li>
     * <li>完成任务依赖关系的演示流程</li>
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
        _log.info("SimpleJob2 提示: " + jobKey + " 正在执行，时间: " + new Date());
    }

}
