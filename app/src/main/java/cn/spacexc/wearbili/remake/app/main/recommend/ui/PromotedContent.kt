package cn.spacexc.wearbili.remake.app.main.recommend.ui

import cn.spacexc.wearbili.remake.app.main.recommend.domain.remote.rcmd.app.Item as AppItem
import cn.spacexc.wearbili.remake.app.main.recommend.domain.remote.rcmd.web.Item as WebItem

/**
 * 推广内容识别。
 *
 * 推荐流里混有广告/推广位，原项目未做任何过滤，用户无法屏蔽。
 * 2026 改版新增「过滤推广内容」开关后，由这里统一判定。
 *
 * 判定依据（两者取并集，宁多勿漏）：
 * - App 接口：`ad_info` 字段非空即推广
 * - Web 接口：`businessInfo` 非空即推广位
 * - 兜底：`goto` 不是 "av" 的通常也不是普通稿件（由调用方另行判断）
 */

fun AppItem.isPromotedContent(): Boolean {
    if (ad_info != null) return true
    // goto 明确指向广告/活动页
    return goto in setOf("ad", "web", "activity")
}

fun WebItem.isPromotedContent(): Boolean {
    if (businessInfo != null) return true
    return goto in setOf("ad", "activity")
}
