# 投屏接收端远程控制 API 文档

投屏接收端提供 HTTP API，用于远程获取当前播放器状态、修改弹幕设置和远程控制播放器。

## 基础信息

- **协议**: HTTP
- **默认端口**: 由应用配置指定
- **认证**: Bearer Token (可选)
  - Header: `Authorization: Bearer <AES加密后的密码>`

## 通用响应格式

```json
{
  "success": true,
  "errorCode": 0,
  "errorMessage": null
}
```

**错误码说明**:
- `200`: 成功
- `400`: 请求参数错误
- `401`: 认证失败
- `404`: API 未找到或播放器未运行
- `500`: 服务器内部错误

---

## API 端点

### 1. 初始化连接

**URL**: `GET /init`

**Headers**:
- `screencast-version`: 投屏协议版本号 (当前版本: 1)

**响应**:
```json
{
  "success": true,
  "errorCode": 200,
  "errorMessage": null
}
```

**错误示例**:
```json
{
  "success": false,
  "errorCode": 409,
  "errorMessage": "投屏版本不匹配，请更新双端至相同APP版本。"
}
```

---

### 2. 修改弹幕设置

用于修改安卓端播放器的弹幕设置。如果播放器正在播放视频，设置会立即生效。

**URL**: `GET /remote/config`

**Query 参数**:

| 参数              | 类型      | 默认值  | 取值范围       | 说明                      |
|-----------------|---------|------|------------|-------------------------|
| danmuSize       | int     | 40   | 0-100      | 弹幕文字大小百分比               |
| danmuSpeed      | int     | 35   | 0-100      | 弹幕速度百分比 (值越小速度越快)       |
| danmuAlpha      | int     | 100  | 0-100      | 弹幕透明度百分比                |
| danmuStoke      | int     | 20   | 0-100      | 弹幕描边宽度百分比               |
| showMobileDanmu | boolean | true | true/false | 显示滚动弹幕                  |
| showBottomDanmu | boolean | true | true/false | 显示底部弹幕                  |
| showTopDanmu    | boolean | true | true/false | 显示顶部弹幕                  |
| danmuMaxCount   | int     | 0    | ≥0         | 弹幕最大同屏数量 (0=无限制)        |
| danmuMaxLine    | int     | -1   | -1或≥0      | 弹幕最大显示行数 (-1=无限制)       |
| cloudDanmuBlock | boolean | true | true/false | 启用弹幕云屏蔽                 |
| danmuLanguage   | int     | 0    | 0-2        | 弹幕语言 (0=原始, 1=简体, 2=繁体) |

**示例**:
```
GET /remote/config?danmuSize=50&danmuSpeed=40&danmuAlpha=90
```

**响应**:
```json
{
  "success": true,
  "errorCode": 200,
  "errorMessage": "弹幕设置已更新"
}
```

---

### 3. 播放器控制

用于查询播放器状态和远程控制播放器操作。

**URL**: 
- `GET /remote/control` - 查询播放器状态
- `POST /remote/control` - 控制播放器操作

#### 3.1 查询播放器状态

**请求**:
```
GET /remote/control
```

**响应**:
```json
{
  "success": true,
  "errorCode": 0,
  "errorMessage": null,
  "status": {
    "title": "Episode 01",
    "playing": true,
    "position": 120000,
    "duration": 1800000,
    "bufferedPercent": 80,
    "speed": 1.0,
    "volumePercent": 75,
    "brightnessPercent": 50,
    "mediaType": "video"
  }
}
```

**status 字段说明**:

| 字段                | 类型      | 说明         |
|-------------------|---------|------------|
| title             | string  | 当前播放视频标题   |
| playing           | boolean | 是否正在播放     |
| position          | long    | 当前播放位置(毫秒) |
| duration          | long    | 视频总时长(毫秒)  |
| bufferedPercent   | int     | 缓冲进度百分比    |
| speed             | float   | 播放速度       |
| volumePercent     | int     | 音量百分比      |
| brightnessPercent | int     | 亮度百分比      |
| mediaType         | string  | 媒体类型       |

#### 3.2 控制操作

**说明**: 使用 POST 方法进行播放器控制，所有控制操作在成功时都会返回当前播放器状态。

**支持的 action**:

| Action | 参数 | 说明 |
|--------|------|------|
| `play`, `resume` | - | 播放/恢复播放 |
| `pause` | - | 暂停播放 |
| `toggle` | - | 切换播放/暂停状态 |
| `seek` | `position` (long) | 跳转到指定位置(毫秒) |
| `seekBy`, `seek_by`, `seekOffset` | `offset` (long) | 相对跳转(毫秒，可为负) |
| `set_speed`, `speed` | `speed` (float) | 设置播放速度 (>0) |
| `set_volume`, `volume` | `percent` (int) | 设置音量百分比 (0-100) |
| `volumeUp`, `volume_up` | `delta` (int, 可选) | 增加音量，默认 +5% |
| `volumeDown`, `volume_down` | `delta` (int, 可选) | 降低音量，默认 -5% |
| `set_brightness`, `brightness` | `percent` (int) | 设置亮度百分比 (0-100) |
| `brightnessUp`, `brightness_up` | `delta` (int, 可选) | 增加亮度，默认 +5% |
| `brightnessDown`, `brightness_down` | `delta` (int, 可选) | 降低亮度，默认 -5% |
| `exit`, `stop` | - | 退出播放器 |

#### 3.3 控制示例

**暂停播放**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"pause"}' \
     http://192.168.1.100:12345/remote/control
```

**播放/恢复播放**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"play"}' \
     http://192.168.1.100:12345/remote/control
```

**跳转到指定位置**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"seek","position":"120000"}' \
     http://192.168.1.100:12345/remote/control
```

**相对跳转（快进30秒）**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"seekBy","offset":"30000"}' \
     http://192.168.1.100:12345/remote/control
```

**设置播放速度**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"speed","speed":"1.5"}' \
     http://192.168.1.100:12345/remote/control
```

**设置音量**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"volume","percent":"80"}' \
     http://192.168.1.100:12345/remote/control
```

**增加音量**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"volumeUp","delta":"10"}' \
     http://192.168.1.100:12345/remote/control
```

**降低音量**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"volumeDown","delta":"10"}' \
     http://192.168.1.100:12345/remote/control
```

**设置亮度**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"brightness","percent":"75"}' \
     http://192.168.1.100:12345/remote/control
```

**增加亮度**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"brightnessUp","delta":"10"}' \
     http://192.168.1.100:12345/remote/control
```

**降低亮度**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"brightnessDown","delta":"10"}' \
     http://192.168.1.100:12345/remote/control
```

**退出播放器**:
```bash
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"exit"}' \
     http://192.168.1.100:12345/remote/control
```

**成功响应**:
```json
{
  "success": true,
  "errorCode": 0,
  "errorMessage": null,
  "status": {
    "title": "Episode 01",
    "playing": false,
    "position": 120000,
    "duration": 1800000,
    "bufferedPercent": 80,
    "speed": 1.0,
    "volumePercent": 75,
    "brightnessPercent": 50,
    "mediaType": "video"
  }
}
```

**错误响应示例**:
```json
{
  "success": false,
  "errorCode": 400,
  "errorMessage": "position 参数无效",
  "status": null
}
```

---

## 完整示例

```bash
# 初始化连接
curl -H "screencast-version: 1" \
     -H "Authorization: Bearer your_token" \
     http://192.168.1.100:12345/init

# 更新弹幕配置
curl -H "Authorization: Bearer your_token" \
     "http://192.168.1.100:12345/remote/config?danmuSize=50&danmuSpeed=40"

# 查询播放器状态
curl -H "Authorization: Bearer your_token" \
     http://192.168.1.100:12345/remote/control

# 暂停播放
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"pause"}' \
     http://192.168.1.100:12345/remote/control

# 播放/恢复播放
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"play"}' \
     http://192.168.1.100:12345/remote/control

# 跳转到指定位置
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"seek","position":"120000"}' \
     http://192.168.1.100:12345/remote/control

# 相对跳转（快进30秒）
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"seekBy","offset":"30000"}' \
     http://192.168.1.100:12345/remote/control

# 设置播放速度
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"speed","speed":"1.5"}' \
     http://192.168.1.100:12345/remote/control

# 调整音量
curl -X POST \
     -H "Authorization: Bearer your_token" \
     -H "Content-Type: application/json" \
     -d '{"action":"volumeUp","delta":"10"}' \
     http://192.168.1.100:12345/remote/control
```

---

## 注意事项

1. **版本检查**: 投屏端和接收端必须使用相同的协议版本 (`screencast-version`)
2. **认证**: 如果接收端设置了密码，所有请求必须携带正确的 Bearer Token
3. **请求方法**: 
   - `/remote/control` 使用 GET 方法查询播放器状态
   - `/remote/control` 使用 POST 方法进行播放器控制操作
   - 弹幕配置 `/remote/config` 使用 GET 方法，参数通过 Query 传递
4. **超时处理**: 控制命令执行超时时间为 2 秒
5. **参数验证**: 所有数值参数会进行边界检查和类型验证

