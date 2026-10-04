package cn.spacexc.wearbili.remake.common.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import cn.spacexc.wearbili.remake.proto.settings.AnimationLevel
import cn.spacexc.wearbili.remake.proto.settings.AppConfiguration
import cn.spacexc.wearbili.remake.proto.settings.Appearance
import cn.spacexc.wearbili.remake.proto.settings.QuickToolBarFunction
import cn.spacexc.wearbili.remake.proto.settings.QuickToolBarSlotCount
import cn.spacexc.wearbili.remake.proto.settings.RecommendSource
import cn.spacexc.wearbili.remake.proto.settings.Theme
import cn.spacexc.wearbili.remake.proto.settings.copy
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

val Context.appConfigurationDataStore: DataStore<AppConfiguration> by dataStore(
    fileName = "app_configuration.pb",
    serializer = AppConfigurationSerializer
)

object AppConfigurationSerializer : Serializer<AppConfiguration> {
    override val defaultValue: AppConfiguration
        get() = AppConfiguration.getDefaultInstance().copy {
            hasAnimation = true
            recommendSource = RecommendSource.Web
            danmakuPlayerSettings = danmakuPlayerSettings.copy {
                alpha = 1f
                displayArea = 1f
                fontScale = 1f
                blockLevel = 0
                isNormalDanmakuEnabled = true
            }
            screenDisplayScaleFactor = 1.0f
            toolBarConfiguration = toolBarConfiguration.copy {
                slotCount = QuickToolBarSlotCount.Two
                functionOne = QuickToolBarFunction.History
                functionTwo = QuickToolBarFunction.Search
            }
            customization = customization.copy {
                recommendPageLargeCard = false
                videoCoverColorAbsorb = true
                theme = Theme.Light
                // 2026 改版新增字段的默认值
                // 注意：原 UI 为硬编码深色，故默认 AlwaysDark 才与实际外观一致
                appearance = Appearance.AlwaysDark
                monetEnabled = true      // API < 31 会自动回落默认粉色方案
                glassEnabled = true
                animationLevel = AnimationLevel.Standard
                themeColorHex = ""
            }
        }


    override suspend fun readFrom(input: InputStream): AppConfiguration {
        try {
            return AppConfiguration.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AppConfiguration, output: OutputStream) {
        t.writeTo(output)
    }
}