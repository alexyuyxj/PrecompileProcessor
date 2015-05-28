# PrecompileProcessor

PrecompileProcessor(下文简称：pcp)是一个古老的工具，它只能在ant上运行，所以不要询问maven或者gradle版本。

本文将分为两个部分，一个介绍如何在ant的编译脚本中使用pcp，一个是介绍pcp在java代码中的宏脚本功能。

## 在ant中使用pcp

### 导入pcp

将pcp的jar包复制到你的编译目录下，打开编译脚本（假定名为“build.xml”）。在任何target之外添加下面的代码：

``` xml
<taskdef 
    classpath="pcp.jar" 
    resource="m/pcp/ant/antlib.xml"/>
```

这里面的classpath是jar包的路径。

### pcp的语法

#### 定义变量

pcp提供了3种定义变量的方法，包括set、list和map。

set不是“集合”的意思，而是“设置”。起作用等价于ant自带的“property”，语法如下：

``` xml
<set name="<名称>" value="<值>"/>
```

其中的value除了是特定的值，也可以是一道表达式，比方说:

``` xml
<set name="x" value="sin(45)"/>
<echo message="${x}"/>
```

输出的不是“sin(45)”这个字符串，而是“0.8509035245341184”。

list是“列表”，语法如下：

``` xml
<!-- 方式1 -->
<list name="<名称>" list="<元素1>,<元素2>,...,<元素n>"/>

<!-- 方式2 -->
<list name="<名称>">
    <item value="<元素1>"/>
    <item value="<元素2>"/>
    ...
    <item value="<元素n>"/>
</list>
```

其中方式1的元素以半角逗号分隔，只能是固定的值。方式二的元素除了是固定值，还可以是一个有结果的数学表达式。

list不能直接被输出，如果要获取数组某一项的值，可以使用

```
${数组名称#元素序号}
```

的方式，如：

``` xml
<list name="list" list="0,1,2,3"/>
<echo message="${list#0}"/>
```

输出的是“0”，因为元素序号从0开始。

list还有另外一个属性，就是“数组长度”，可以通过下面的方法获取：

```
${数组名#size}
```

map是“表格”，或者说是“kv对”。定义语法如下：

``` xml
<map name="<名称>">
    <item name="<元素名称>" value="<元素值>"/>
</map>
```

元素的值也可以是数学表达式。

map同样具备“size”的属性，得到map的容量。要获取map的元素，可以这样子：

```
${表格名称#元素名称}
```