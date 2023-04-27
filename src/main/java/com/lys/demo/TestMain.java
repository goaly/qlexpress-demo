/**
 * Copyright (C), 2022-2032
 */
package com.lys.demo;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;

/**
 * TestMain
 *
 * @author: lys
 * @date: 2022/6/16 21:17
 */
@Slf4j
public class TestMain {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private static final String KEYWORD_ELSE = " else ";

    /**
     * 实际达成率公式
     */
    private static final String FORMULA_ACTUAL_REACH_RATE = "实际完成值/目标值";

    public static void main(String[] args) throws Exception {

//        testScoreFormula();
        /** 复杂逻辑表达式 */
        ExpressRunner runner_3 = new ExpressRunner();
        runner_3 = new ExpressRunner(true, false);
        runner_3.addOperatorWithAlias("如果", "if", null);
        runner_3.addOperatorWithAlias("或", "||", null);
        runner_3.addOperatorWithAlias("且", "&&", null);
        runner_3.addOperatorWithAlias("等于", "==", null);
        runner_3.addOperatorWithAlias("大于", ">", null);
        runner_3.addOperatorWithAlias("大于等于", ">=", null);
        runner_3.addOperatorWithAlias("小于", "<", null);
        runner_3.addOperatorWithAlias("则", "then", null);
        runner_3.addOperatorWithAlias("否则", "else", null);
        runner_3.addOperatorWithAlias("返回", "return", null);
        runner_3.addFunctionOfClassMethod("获取JSON中的值", TestMain.class.getName(), "getValue", new String[]{"String"},
                null);
        runner_3.addFunctionOfClassMethod("字符串等于", TestMain.class.getName(), "equals",
                new String[]{"String", "String"}, null);


        String dataStr = "{\"权重\":25,\"目标值\":16.130000,\"actualValue\":17.700000,\"weight\":25,\"实际达成率\":1.097334,\"保底达成率\":0.9600,\"actualReachRate\":1.097334,\"targetRate\":1.0000,\"scoreLowerLimit\":0.00,\"targetValue\":16.130000,\"scoreUpperLimit\":120.00,\"guaranteedRate\":0.9600,\"计分下限\":0.00,\"目标达成率\":1.0000,\"计分上限\":120.00,\"实际值\":17.700000}";
        DefaultContext<String, Object> context_3_1 = JSONObject.parseObject(dataStr, DefaultContext.class);
        String express_3_1 = "if(实际达成率＜保底达成率){return 权重*0.8-(保底达成率-实际达成率)*权重*(1-0.8)/(目标达成率-保底达成率);} else if(实际达成率>=保底达成率 and 实际达成率<=目标达成率){return 权重*实际达成率/目标达成率;} else if(实际达成率＞目标达成率){return 权重+(实际达成率-目标达成率)*权重/目标达成率*2;}";
        Object result_3_1 = runner_3.execute(express_3_1, context_3_1, null, true, false);
        System.out.println(result_3_1);


    }

    public static Object getValue(String name) {
        String json = "{\"code\": 2,\"message\": \"success\"}";
        return JSONObject.parseObject(json).get(name);
    }

    public static boolean equals(String param1, String param2) {
        return StrUtil.equals(param1, param2);
    }

    private static void testScoreFormula() throws Exception {
    /*
   1、实际达成率<80%，此项绩效计分=权重*80%-权重*(80%-实际完成值)*2；
   2、80%<=实际达成率<100%，此项绩效计分=权重*实际完成值；
   3、100%<=实际达成率<120%，此项绩效计计分=权重+权重*(实际完成值-100%)*2。
   4、实际达成率>=120%，此项绩效计计分=权重+权重*(实际完成值-120%)*2+权重*(实际完成值-120%)*0.5，权重*150%封顶。
   */
        String express1 = "if (实际达成率<保底达成率) {return 权重*保底达成率-权重*(保底达成率-实际达成率)*2}";
        String express2 = "if (实际达成率>=保底达成率 && 实际达成率<目标达成率) {return 权重*实际达成率}";
        String express3 = "if (实际达成率>=目标达成率 && 实际达成率<挑战达成率) {return 权重+权重*(实际达成率-目标达成率)*2}";
        String express4 = "if (实际达成率>=挑战达成率) {return 权重+权重*(实际达成率-挑战达成率)*2+权重*(实际达成率-挑战达成率)*0.5}";

        ExpressRunner runner = new ExpressRunner(true, false);
        DefaultContext<String, Object> context = new DefaultContext<>();

        // 权重
        int weight = 20;
        // 保底达成率
        BigDecimal minReachRate = new BigDecimal("0.8");
        // 目标达成率
        BigDecimal targetReachRate = BigDecimal.ONE;
        // 挑战达成率
        BigDecimal challengeReachRate = new BigDecimal("1.2");
        // 上限比例
        BigDecimal upperLimitRate = new BigDecimal("1.5");
        // 目标值
        BigDecimal targetValue = new BigDecimal("500.00");

        // 计分上限
        BigDecimal scoreUpperLimit = new BigDecimal(weight).multiply(upperLimitRate).setScale(2);

        log.info(String.format("权重%d 保底达成率 %s%%, 目标达成率 %s%%, 挑战达成率 %s%%, 目标值 %s万元",
                weight,
                minReachRate.multiply(ONE_HUNDRED).toPlainString(),
                targetReachRate.multiply(ONE_HUNDRED).toPlainString(),
                challengeReachRate.multiply(ONE_HUNDRED).toPlainString(),
                targetValue.toPlainString()
        ));
        Scanner input = new Scanner(System.in);
        System.out.print("请输入实际完成值（万元）：");
        BigDecimal actualValue = new BigDecimal(input.next());
        context.clear();
        context.put("实际完成值", actualValue);
        context.put("目标值", targetValue);
        // 实际达成率
        BigDecimal actualReachRate = (BigDecimal) runner.execute(FORMULA_ACTUAL_REACH_RATE, context, null,
                true, false);
        log.info(String.format("实际达成率: %s%%", actualReachRate.multiply(ONE_HUNDRED).setScale(2).toPlainString()));

        context.clear();
        context.put("权重", weight);
        context.put("保底达成率", minReachRate);
        context.put("目标达成率", targetReachRate);
        context.put("实际达成率", actualReachRate);
        context.put("挑战达成率", challengeReachRate);

        StringBuilder strBuf = new StringBuilder(express1)
                .append(KEYWORD_ELSE)
                .append(express2)
                .append(KEYWORD_ELSE)
                .append(express3)
                .append(KEYWORD_ELSE)
                .append(express4);
        BigDecimal result = (BigDecimal) runner.execute(strBuf.toString(), context, null, true, false);

        if (scoreUpperLimit != null && result.compareTo(scoreUpperLimit) > 0) {
            log.warn("得分{}超过上限{}，将按上限计分...", result.toPlainString(), scoreUpperLimit.toPlainString());
            result = scoreUpperLimit;
        }

        log.info("得分：" + result.setScale(2).toPlainString());
    }
}
