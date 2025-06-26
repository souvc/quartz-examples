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

package org.quartz.examples.example4;

import java.util.Date;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 这是一个简单的任务类，演示如何接收参数和维护状态
 * </p>
 * <p>
 * 本类展示了以下重要概念：
 * 1. 通过 JobDataMap 接收和维护任务参数
 * 2. 使用 @PersistJobDataAfterExecution 持久化任务数据
 * 3. 使用 @DisallowConcurrentExecution 防止并发执行
 * 4. 实例变量与任务状态的区别
 * </p>
 *
 * @author Bill Kratzer
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ColorJob implements Job {

    private static Logger _log = LoggerFactory.getLogger(ColorJob.class);

    // 任务特定的参数名称常量
    // 喜欢的颜色参数
    public static final String FAVORITE_COLOR = "favorite color";
    // 执行次数参数
    public static final String EXECUTION_COUNT = "count";

    // 由于 Quartz 每次执行任务时都会重新实例化类，
    // 非静态成员变量无法用于维护状态！
    // 这个变量仅用于演示目的，实际状态应该通过 JobDataMap 维护
    private int _counter = 1;

    /**
     * <p>
     * 任务初始化的空构造函数
     * </p>
     * <p>
     * Quartz 要求任务类必须有一个公共的无参构造函数，
     * 以便调度器在需要时能够实例化该类。
     * </p>
     */
    public ColorJob() {
    }

    /**
     * <p>
     * 当与任务关联的触发器触发时，由调度器调用此方法
     * </p>
     * <p>
     * 此方法演示了：
     * 1. 如何从 JobDataMap 中读取参数
     * 2. 如何更新任务状态并保存回 JobDataMap
     * 3. 实例变量与持久化状态的区别
     * </p>
     *
     * @throws JobExecutionException
     *             如果任务执行过程中发生异常
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        // 获取任务的唯一标识符
        // JobKey 包含任务名称和组名，用于唯一标识一个任务
        JobKey jobKey = context.getJobDetail().getKey();

        // 从 JobDataMap 中获取传递的参数
        // JobDataMap 是 Quartz 中用于在任务执行之间传递和维护数据的机制
        JobDataMap data = context.getJobDetail().getJobDataMap();
        // 获取喜欢的颜色
        String favoriteColor = data.getString(FAVORITE_COLOR);
        // 获取执行次数
        int count = data.getInt(EXECUTION_COUNT);
        // 打印任务执行信息，展示参数值和状态
        _log.info("ColorJob: {} 执行时间: {}\n  喜欢的颜色: {}\n  执行次数 (来自 JobDataMap): {}\n  执行次数 (来自实例变量): {}", new Object[]{jobKey, new Date(), favoriteColor, count, _counter});

        // 增加执行次数并将其存储回 JobDataMap
        // 这是维护任务状态的正确方式
        // @PersistJobDataAfterExecution 注解确保这些更改会被持久化
        count++;
        data.put(EXECUTION_COUNT, count);

        // 增加本地成员变量
        // 这没有实际意义，因为任务状态无法通过成员变量维护！
        // 每次任务执行时，Quartz 都会创建新的类实例，
        // 所以实例变量的值不会在执行之间保持
        _counter++;
    }

}
