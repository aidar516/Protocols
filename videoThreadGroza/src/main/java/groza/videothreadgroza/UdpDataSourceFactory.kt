package groza.videothreadgroza

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.UdpDataSource

@UnstableApi
class UdpDataSourceFactory : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return UdpDataSource(64 * 1024, 5000)
    }
}
