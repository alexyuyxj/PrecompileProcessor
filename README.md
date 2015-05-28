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
<echo message="${list#1}"/>
```

输出的是“1”，因为元素序号从0开始。

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

map同样具备“size”的属性，通过它可以得到map的容量。若要获取map的元素，可以这样子：

```
${表格名称#元素名称}
```

如：

``` xml
<map name="book-prices">
    <item name="java" value="12"/>
    <item name="c" value="13"/>
</map>
<echo message="${book-prices#java}"/>
```

输出的是“12”。

### 修改变量

pcp提供了3中变量修改方法，分别对应不同的数据类型。

首先是set。set除了可以定义变量，还能修改变量的值，一般操作如下：

``` xml
<!-- 修改属性 -->
<set name="text" value="1222"/>

<!-- 修改数组元素 -->
<set name="list#2" value="22222"/>

<!-- 修改map元素 -->
<set name="map#key" value="value-value"/>
```

其次是专门用于追加数组元素的add，语法如下：

``` xml
<add list="<数组名称>" value="<新的元素>"/>
```

此方法总会将新元素追加到指定数组的尾部，并且增加其size属性。

最后是put，专门用于往map中添加数据，语法如下：

``` xml
<put map="<表格名称>" name="元素名称" value="<元素的值>"/>
```

put不仅可以往map中增加元素，还能用来修改旧有元素的值。

### 分支语句

pcp提供了3中结构化分支语句：if、for和while。并且还为for和while提供了跳出/跳过循环的break和continue。

其中if的语法为：

``` xml
<if condition="<判断条件>">
    <then>
        <!-- 在此处添加判断条件成立的语句 -->
    </then>
    <else>
        <!-- 在此处添加判断条件不成立的语句 -->
    </else>
</if>
```

如：

``` xml
<if condition="${x} == 10">
    <then>
        <echo message="x = 10"/>
    </then>
    <else>
        <echo message="x != 10"/>
    </else>
</if>
```

其中then和else块都可以嵌套更多if或其他分支命令。

for有3种格式，分别为：

``` xml
<!-- 指定循环次数 -->
<for iterator="<迭代器名称>" limit="<循环次数>">
    <!-- 在此处添加要循环执行的语句 -->
</for>
```

在循环中可以通过

```
${迭代器名称#index}
```

得到当前循环的序号，其值从0到（limit - 1）。

``` xml
<!-- 指定列表迭代 -->
<for iterator="<迭代器名称>" list="<列表名称>">
    <!-- 在此处添加要循环执行的语句 -->
</for>
```

循环会执行"列表#size"次数，在循环体中，可以使用

```
${迭代器名称#index}
```

获取当前循环序号，或者使用

```
${迭代器名称#value}
```

获取当前循环迭代器中值。

``` xml
<!-- 指定map迭代 -->
<for iterator="<迭代器名称>" map="<表格名称>">
    <!-- 在此处添加要循环执行的语句 -->
</for>
```

循环会执行"表格#size"次数，在循环体中，可以使用

```
${迭代器名称#name}
```

获取当前循环迭代器中的元素名称，或者使用

```
${迭代器名称#value}
```

获取当前循环迭代器中元素的值。

在循环体中的任何地方可以使用

``` xml
<break/>
```

来跳出当前循环。或者使用

``` xml
<continue/>
```

来跳过本次循环，进入下一次循环。

最后是while：

``` xml
<while condition="<继续循环的条件>">
    <!-- 在此处添加要循环执行的语句 -->
</while>
```

如果condition存在，它可以是固定值，或者返回布尔值的数学表达式。如果不存在，则默认为true。

while也可以使用break和continue。
