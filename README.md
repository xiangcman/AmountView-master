# 银行类app额度view
<div>
    <image src="https://github.com/1002326270xc/AmountView-master/blob/master/photos/掌上生活额度控件.gif" width="250"/>
    <image src="https://github.com/1002326270xc/AmountView-master/blob/master/photos/自己撸的额度控件.gif" width="250"/>
</div>

### 使用:

**xml:**
```
<com.xiangcheng.amount.AmountView
    android:id="@+id/amount_view"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_centerInParent="true"
    app:hint_text="Max Amount"
    app:max_amount="30000"
    app:shadow_color="#FFD700" />
```
这里就定义了三个属性:

**hint_text:** 提示文字

**max_amount:** 最大的额度

**shadow_color:** 背景颜色

**code:**
```
//代码中设置额度
public void setAmount(int amount) {
}
```
```
//启动动画
public void start() {
}
```

**gradle dependence:**

`compile 'com.a1002326270:amountlibs:1.0'`

**欢迎大家提出问题，留言板留言或邮箱直接联系我。我会第一时间测试相关的bug**

**欢迎客官到本店光临(qq群):**

<image src="https://github.com/1002326270xc/LayoutManager-FlowLayout/blob/master/photos/IMG_0221.jpg" width="250" width="250" title="qq群"/>

### 关于我:

**email:** a1002326270@163.com
