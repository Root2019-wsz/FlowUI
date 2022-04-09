package com.shenzhe.flowui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {
    //所有的子控件容器
    List<List<View>> list = new ArrayList<>();
    //每一行行高存起来
    List<Integer> listLineHeight = new ArrayList<>();

    //防止测量多次
    private boolean isMeasure = false;

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //重写 MarginLayoutParams
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取父控件给的一个参考值
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //获取到自己的测量模式
        // 在LinearLayout中measureChildBeforeLayout中把测量规则改成自view的
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //保存当前控件里面的子控件的总宽高
        int childCountWidth = 0;
        int childCountHeight = 0;

        if(!isMeasure){
            isMeasure = true;
        }else{
            //当前控件中字控件一行使用的宽度值
            int lineCountWidth = 0;

            //保存一行中最高子控件
            int lineMaxHeight = 0;

            //存储每个子控件的宽高
            int iChildWidth = 0;
            int iChildHeight = 0;

            //创建一行的容器
            List<View> viewList = new ArrayList<>();

            //遍历所有子控件
            int childCount = getChildCount();
            for (int x = 0; x < childCount; x++) {
                //获得子控件
                View childAt = getChildAt(x);
                //先测量子控件
                measureChild(childAt,widthMeasureSpec,heightMeasureSpec);
                //从子控件中获取LayoutParams
                MarginLayoutParams layoutParams = (MarginLayoutParams)childAt.getLayoutParams();
                //计算当前控件实际宽高
                iChildWidth = childAt.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
                iChildHeight = childAt.getMeasuredHeight() + layoutParams.bottomMargin + layoutParams.topMargin;
                //当子控件宽度累加后是否大于父控件
                if (iChildWidth +lineCountWidth > widthSize){
                    //需要换行，保存一行的信息
                    //每次换行的时候比较当前行和上一行谁的宽度大
                    childCountWidth = Math.max(lineCountWidth,childCountWidth);
                    //如果需要换行，累加行高
                    childCountHeight += lineMaxHeight;
                    //把行高记录到集合中
                    listLineHeight.add(lineMaxHeight);
                    //把一行的数据放进总容器
                    list.add(viewList);
                    //把一行的容器重新创建一个，新的一行
                    viewList = new ArrayList<>();
                    //将每一行总宽高重新初始化
                    lineCountWidth = iChildWidth;
                    lineMaxHeight = iChildHeight;

                    viewList.add(childAt);
                }else {
                    lineCountWidth += iChildWidth;
                    //对比每个子控件高度谁最高
                    lineMaxHeight = Math.max(lineMaxHeight , iChildHeight);
                    //如果不需要换行，保存在一行中
                    viewList.add(childAt);
                }
                //这样做的原因是  之前的if else中 不会把最后一行的高度加进listLineHeight
                // 最后一行要特殊对待 不管最后一个item是不是最后一行的第一个item
                if(x == childCount - 1){
                    //保存当前行信息
                    childCountWidth = Math.max(lineCountWidth,childCountWidth);
                    childCountHeight +=lineMaxHeight;

                    listLineHeight.add(lineMaxHeight);
                    list.add(viewList);
                }
            }
        }

        //设置控件最终的大小
        int measureWidth = widthMode == MeasureSpec.EXACTLY?widthSize:childCountWidth;
        int measureHeight = heightMode == MeasureSpec.EXACTLY?heightSize:childCountHeight;
        setMeasuredDimension(measureWidth,measureHeight);

    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        //摆放子控件位置
        int left,right,top,bottom;
        //保存上一个控件边距
        int countLeft = 0;
        //保存上一行的高度和边距
        int countTop = 0;
        //遍历所有行
        for (List<View> views : list) {
            //遍历每一行的控件
            for (View view : views) {
                //获取到控件的属性对象
                MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
                left = countLeft+layoutParams.leftMargin;
                top = countTop + layoutParams.topMargin;
                right = left+view.getMeasuredWidth();
                bottom = top+view.getMeasuredHeight();
                view.layout(left,top,right,bottom);

                countLeft += view.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
            }
            //获取到当前这一行的position
            int index = list.indexOf(views);
            countLeft = 0;
            countTop+= listLineHeight.get(index);
        }
        list.clear();
        listLineHeight.clear();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
