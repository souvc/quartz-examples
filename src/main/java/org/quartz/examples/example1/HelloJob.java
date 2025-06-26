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
 
package org.quartz.examples.example1;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <p>
 * 这是一个简单的任务，向世界说"Hello"。
 * </p>
 * 
 * @author Bill Kratzer
 */
public class HelloJob implements Job {

    // 静态日志记录器，用于记录任务执行信息
    private static Logger _log = LoggerFactory.getLogger(HelloJob.class);

    /**
     * <p>
     * 用于任务初始化的空构造函数
     * </p>
     * <p>
     * Quartz要求有一个公共的空构造函数，这样调度器就可以在需要时实例化该类。
     * </p>
     */
    public HelloJob() {
    }

    /**
     * <p>
     * 当与该<code>Job</code>关联的<code>{@link org.quartz.Trigger}</code>触发时，
     * 由<code>{@link org.quartz.Scheduler}</code>调用此方法。
     * </p>
     * 
     * @throws JobExecutionException
     *             如果在执行任务时发生异常。
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        // 向世界说Hello并显示当前日期/时间
        _log.info("Hello World! - " + new Date());
    }

}
