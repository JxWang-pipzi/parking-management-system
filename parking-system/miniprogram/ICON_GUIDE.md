# 微信小程序图标生成指南

## 快速解决方案

### 方法一：使用在线工具（推荐）

1. 打开浏览器，访问项目目录下的 `download-icons.html` 文件
2. 点击每个图标下方的"下载PNG"按钮
3. 将下载的PNG文件保存到 `miniprogram/images/` 目录

### 方法二：使用微信开发者工具

1. 打开微信开发者工具
2. 在项目中创建 `images` 目录
3. 使用开发者工具的图片编辑功能创建图标

### 方法三：使用图片编辑软件

1. 使用 Photoshop、Figma 或其他设计工具
2. 创建 81x81 像素的图标
3. 导出为 PNG 格式
4. 保存到 `miniprogram/images/` 目录

## 所需图标列表

请确保以下图标文件存在于 `miniprogram/images/` 目录：

| 文件名 | 用途 | 尺寸 | 颜色 |
|--------|------|------|------|
| home.png | 首页图标（未选中） | 81x81 | 灰色 #999999 |
| home-active.png | 首页图标（选中） | 81x81 | 蓝色 #409EFF |
| parking.png | 停车场图标（未选中） | 81x81 | 灰色 #999999 |
| parking-active.png | 停车场图标（选中） | 81x81 | 蓝色 #409EFF |
| order.png | 订单图标（未选中） | 81x81 | 灰色 #999999 |
| order-active.png | 订单图标（选中） | 81x81 | 蓝色 #409EFF |
| profile.png | 我的图标（未选中） | 81x81 | 灰色 #999999 |
| profile-active.png | 我的图标（选中） | 81x81 | 蓝色 #409EFF |

## 图标设计规范

### 尺寸要求
- 推荐：81x81 像素
- 最小：40x40 像素
- 最大：120x120 像素

### 颜色规范
- 未选中状态：#999999（灰色）
- 选中状态：#409EFF（蓝色）

### 设计要点
1. 图标应简洁明了
2. 保持视觉一致性
3. 确保在小尺寸下清晰可辨
4. 避免过多细节

## 临时解决方案

如果暂时没有图标文件，可以修改 `app.json`，使用文字作为临时方案：

```json
{
  "tabBar": {
    "custom": true,
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "首页"
      },
      {
        "pagePath": "pages/parking-lots/parking-lots",
        "text": "停车场"
      },
      {
        "pagePath": "pages/orders/orders",
        "text": "订单"
      },
      {
        "pagePath": "pages/profile/profile",
        "text": "我的"
      }
    ]
  }
}
```

## 使用 iconfont（可选）

如果不想使用图片图标，可以使用 iconfont：

1. 访问 [iconfont.cn](https://www.iconfont.cn/)
2. 搜索需要的图标
3. 下载 PNG 格式
4. 重命名并保存到 `images` 目录

## 常见问题

### Q: 图标显示模糊怎么办？
A: 确保图标尺寸为 81x81 像素或更大，使用 2x 或 3x 图标。

### Q: 图标颜色不对怎么办？
A: 检查图标文件是否正确，确保未选中状态为灰色，选中状态为蓝色。

### Q: 图标不显示怎么办？
A: 检查文件路径是否正确，确保文件名与 app.json 中的配置一致。

## 自动生成脚本

项目提供了自动生成脚本：

```bash
# 生成 SVG 图标
node generate-icons.js

# 然后在浏览器中打开 download-icons.html 下载 PNG 图标
```

## 联系支持

如有问题，请联系开发团队。
