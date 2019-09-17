package com.tttangerine.availableseat.view;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.tttangerine.availableseat.R;
import com.tttangerine.availableseat.activity.ChooseSeatActivity;

import java.util.ArrayList;

import static com.tttangerine.availableseat.db.Seat.SEAT_TYPE_AVAILABLE;
import static com.tttangerine.availableseat.db.Seat.SEAT_TYPE_NOT_AVAILABLE;
import static com.tttangerine.availableseat.db.Seat.SEAT_TYPE_SELECTED;
import static com.tttangerine.availableseat.db.Seat.SEAT_TYPE_USED;
import static com.tttangerine.availableseat.db.Seat.SEAT_TYPE_WAITING;

public class ChooseSeatView extends View {
    private final boolean DBG = false;

    Paint paint = new Paint();
    Paint overviewPaint = new Paint();
    Paint lineNumberPaint;
    Paint.FontMetrics lineNumberPaintFontMetrics;
    float lineNumberTxtHeight;


    //用来保存所有行号
    ArrayList<String> lineNumbers = new ArrayList<>();

    /**
     * 设置行号 默认显示 1,2,3....数字
     * @param lineNumbers  行号
     */
    /*public void setLineNumbers(ArrayList<String> lineNumbers){
        this.lineNumbers = lineNumbers;
        invalidate();
    }*/

    //座位矩阵
    Matrix matrix = new Matrix();

    //座位水平间距
    int spacing;

    //座位垂直间距
    int verSpacing;

    //行号宽度
    int numberWidth;

    //行数
    int row;

    //列数
    int column;

    //可选时座位的图片
    Bitmap availableSeatBitmap;

    //选中时座位的图片
    Bitmap checkedSeatBitmap;

    //座位已被占用时的图片
    Bitmap usedSeatBitmap;

    //用户暂时离开时的图片
    Bitmap waitingSeatBitmap;

    //标识是否需要绘制座位图
    boolean isNeedDrawSeatBitmap = true;

    //整个座位图的宽度
    int seatBitmapWidth;

    //整个座位图的高度
    int seatBitmapHeight;

    //标识是否需要绘制概览图
    boolean isDrawOverview = false;

    //标识是否需要更新概览图
    boolean isDrawOverviewBitmap = true;

    //座位概览图
    Bitmap overviewBitmap;

    //整个概览图的宽度
    float rectW;

    //整个概览图的高度
    float rectH;

    //概览图内白色方块高度
    float rectHeight;

    //概览图内白色方块的宽度
    float rectWidth;

    //概览图内方块的水平间距
    float overviewSpacing;

    //概览图内方块的垂直间距
    float overviewVerSpacing;

    //概览图的比例
    float overviewScale = 4.8f;

    //是否第一次执行onDraw
    //boolean isFirstDraw = true;

    //标识是否正在缩放
    boolean isScaling;
    float scaleX, scaleY;

    //是否是第一次缩放
    boolean firstScale = true;

    int overview_checked;
    int overview_used;
    int overview_waiting;

    int seatAvailableResID;
    int seatCheckedResID;
    int seatUsedResID;
    int seatWaitingResID;

    int txt_color;

    boolean isOnClick;

    /*
    //座位已被占用
    private static final int SEAT_TYPE_USED = 1;

    //座位已经选中
    private static final int SEAT_TYPE_SELECTED = 2;

    //座位可选
    private static final int SEAT_TYPE_AVAILABLE = 3;

    //座位不可用
    private static final int SEAT_TYPE_NOT_AVAILABLE = 4;

    //用户暂时离座
    private static final int SEAT_TYPE_WAITING = 5;
    */


    //顶部说明栏（“可选”、“已选”、“已占用”、“暂离”）
    Paint headPaint;
    Bitmap headBitmap;

    // 顶部说明栏高度
    float headHeight;

    //头部下面横线的高度
    int borderHeight = 1;
    Paint redBorderPaint;

    //座位图片的宽度
    private int seatWidth;

    //座位图片的高度
    private int seatHeight;

    public ChooseSeatView(Context context){ super(context); }

    public ChooseSeatView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context,attrs);
    }

    public ChooseSeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    //传入上下文和自定义控件属性进行初始化
    private void init(Context context,AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChooseSeatView);
        overview_checked = typedArray.getColor(R.styleable.ChooseSeatView_overview_checked, Color.parseColor("#5A9E64"));
        overview_used = typedArray.getColor(R.styleable.ChooseSeatView_overview_used, Color.RED);
        overview_waiting = typedArray.getColor(R.styleable.ChooseSeatView_overview_waiting, Color.BLUE);
        seatCheckedResID = typedArray.getResourceId(R.styleable.ChooseSeatView_seat_checked, R.drawable.seat_green);
        seatUsedResID = typedArray.getResourceId(R.styleable.ChooseSeatView_seat_used, R.drawable.seat_red);
        seatAvailableResID = typedArray.getResourceId(R.styleable.ChooseSeatView_seat_available, R.drawable.seat_gray);
        seatWaitingResID = typedArray.getResourceId(R.styleable.ChooseSeatView_seat_waiting, R.drawable.seat_blue);
        txt_color=typedArray.getColor(R.styleable.ChooseSeatView_txt_color,Color.WHITE);
        typedArray.recycle();
        zoomAnimate(getMatrixScaleX(), 2.0f);
    }

    //控制缩放
    float xScale1 = 1;
    float yScale1 = 1;

    //实现抗锯齿
    Paint pathPaint;
    RectF rectF;

    //dip转化为pix像素值
    private float dip2Px(float value) {
        return getResources().getDisplayMetrics().density * value;
    }

    private void init() {

        spacing = (int) dip2Px(5);
        verSpacing = (int) dip2Px(10);
        //defaultScreenWidth = (int) dip2Px(80);

        availableSeatBitmap = BitmapFactory.decodeResource(getResources(), seatAvailableResID);

        //默认的座位图宽度,如果使用的自己的座位图片比这个尺寸大或者小,会缩放到这个大小
        float defaultImgW = 40;
        float scaleX = defaultImgW / availableSeatBitmap.getWidth();
        //默认的座位图高度
        float defaultImgH = 34;
        float scaleY = defaultImgH / availableSeatBitmap.getHeight();
        xScale1 = scaleX;
        yScale1 = scaleY;

        seatHeight= (int) (availableSeatBitmap.getHeight()*yScale1);
        seatWidth= (int) (availableSeatBitmap.getWidth()*xScale1);

        checkedSeatBitmap = BitmapFactory.decodeResource(getResources(), seatCheckedResID);
        usedSeatBitmap = BitmapFactory.decodeResource(getResources(), seatUsedResID);
        waitingSeatBitmap = BitmapFactory.decodeResource(getResources(), seatWaitingResID);

        seatBitmapWidth = (int) (column * availableSeatBitmap.getWidth()*xScale1 + (column - 1) * spacing);
        seatBitmapHeight = (int) (row * availableSeatBitmap.getHeight()*yScale1 + (row - 1) * verSpacing);
        paint.setColor(Color.RED);
        numberWidth = (int) dip2Px(20);

        //screenHeight = dip2Px(20);
        headHeight = dip2Px(40);

        headPaint = new Paint();
        headPaint.setStyle(Paint.Style.FILL);
        headPaint.setTextSize(40);
        headPaint.setColor(Color.WHITE);
        headPaint.setAntiAlias(true);  //抗锯齿

        //ANTI_ALIAS_FLAG抗锯齿
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setColor(Color.parseColor("#e2e2e2"));

        redBorderPaint = new Paint();
        redBorderPaint.setAntiAlias(true);
        redBorderPaint.setColor(Color.RED);
        redBorderPaint.setStyle(Paint.Style.STROKE);
        redBorderPaint.setStrokeWidth(getResources().getDisplayMetrics().density * 1);

        rectF = new RectF();

        rectHeight = seatHeight / overviewScale;
        rectWidth = seatWidth / overviewScale;
        overviewSpacing = spacing / overviewScale;
        overviewVerSpacing = verSpacing / overviewScale;

        rectW = column * rectWidth + (column - 1) * overviewSpacing + overviewSpacing * 2;
        rectH = row * rectHeight + (row - 1) * overviewVerSpacing + overviewVerSpacing * 2;
        overviewBitmap = Bitmap.createBitmap((int) rectW, (int) rectH, Bitmap.Config.ARGB_4444);

        lineNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lineNumberPaint.setColor(bacColor);
        lineNumberPaint.setTextSize(getResources().getDisplayMetrics().density * 16);
        lineNumberTxtHeight = lineNumberPaint.measureText("4");
        lineNumberPaintFontMetrics = lineNumberPaint.getFontMetrics();
        lineNumberPaint.setTextAlign(Paint.Align.CENTER);

        if(lineNumbers==null){
            lineNumbers=new ArrayList<>();
        }else if(lineNumbers.size()<=0) {
            for (int i = 0; i < row; i++) {
                lineNumbers.add((i + 1) + "");
            }
        }

        matrix.postTranslate(numberWidth + spacing, headHeight + /*screenHeight + */borderHeight + verSpacing);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        long startTime = System.currentTimeMillis();
        if (row <= 0 || column == 0) {
            return;
        }

        drawSeat(canvas);
        drawNumber(canvas);

        if (headBitmap == null) {
            headBitmap = drawHeadInfo();
        }
        canvas.drawBitmap(headBitmap, 0, 0, null);

        //drawScreen(canvas);

        if (isDrawOverview) {
            long s = System.currentTimeMillis();
            if (isDrawOverviewBitmap) {
                drawOverview();
            }
            canvas.drawBitmap(overviewBitmap, 0, 0, null);
            drawOverview(canvas);
            Log.d("drawTime", "OverviewDrawTime:" + (System.currentTimeMillis() - s));
        }

        if (DBG) {
            long drawTime = System.currentTimeMillis() - startTime;
            Log.d("drawTime", "totalDrawTime:" + drawTime);
        }
    }

    int lastX, lastY;
    private int downX, downY;
    private boolean pointer;

    public boolean performClick(){ return super.performClick(); }


    Handler handler = new Handler();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        int x = (int) event.getX();
        super.onTouchEvent(event);

        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        if (pointerCount > 1) {
            pointer = true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointer = false;
                downX = x;
                downY = y;
                isDrawOverview = true;
                handler.removeCallbacks(hideOverviewRunnable);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isScaling && !isOnClick) {
                    int downDX = Math.abs(x - downX);
                    int downDY = Math.abs(y - downY);
                    if ((downDX > 10 || downDY > 10) && !pointer) {
                        int dx = x - lastX;
                        int dy = y - lastY;
                        matrix.postTranslate(dx, dy);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                handler.postDelayed(hideOverviewRunnable, 1500);

                autoScale();
                int downDX = Math.abs(x - downX);
                int downDY = Math.abs(y - downY);
                if ((downDX > 10 || downDY > 10) && !pointer) {
                    autoScroll();
                }
                break;
        }
        isOnClick = false;
        lastY = y;
        lastX = x;

        return true;
    }

    private Runnable hideOverviewRunnable = new Runnable() {
        @Override
        public void run() {
            isDrawOverview = false;
            invalidate();
        }
    };

    Matrix tempMatrix = new Matrix();

    Bitmap drawHeadInfo() {
        String txt = "已占";
        float txtY = getBaseLine(headPaint, 0, headHeight);
        int txtWidth = (int) headPaint.measureText(txt);
        float spacing = dip2Px(10);
        float spacing1 = dip2Px(5);
        /*float y = (headHeight - availableSeatBitmap.getHeight()) / 2;*/

        float width = availableSeatBitmap.getWidth() + spacing1 + txtWidth + spacing +
                usedSeatBitmap.getWidth() + spacing1 + txtWidth + spacing +
                checkedSeatBitmap.getHeight() + spacing1 + txtWidth + spacing +
                waitingSeatBitmap.getWidth() + spacing1 + txtWidth;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), (int) headHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        //绘制背景
        canvas.drawRect(0, 0, getWidth(), headHeight, headPaint);
        headPaint.setColor(Color.BLACK);

        float startX = (getWidth() - width) / 2;
        tempMatrix.setScale(xScale1,yScale1);
        tempMatrix.postTranslate(startX,(headHeight - seatHeight) / 2);
        canvas.drawBitmap(availableSeatBitmap, tempMatrix, headPaint);
        canvas.drawText("可选", startX + seatWidth + spacing1, txtY, headPaint);

        float usedSeatBitmapX = startX + availableSeatBitmap.getWidth() + spacing1 + txtWidth + spacing;
        tempMatrix.setScale(xScale1,yScale1);
        tempMatrix.postTranslate(usedSeatBitmapX,(headHeight - seatHeight) / 2);
        canvas.drawBitmap(usedSeatBitmap, tempMatrix, headPaint);
        canvas.drawText("已占", usedSeatBitmapX + seatWidth + spacing1, txtY, headPaint);

        float checkedSeatBitmapX = usedSeatBitmapX + usedSeatBitmap.getWidth() + spacing1 + txtWidth + spacing;
        tempMatrix.setScale(xScale1,yScale1);
        tempMatrix.postTranslate(checkedSeatBitmapX,(headHeight - seatHeight) / 2);
        canvas.drawBitmap(checkedSeatBitmap, tempMatrix, headPaint);
        canvas.drawText("已选", checkedSeatBitmapX + spacing1 + seatWidth, txtY, headPaint);

        float waitingSeatBitmapX = checkedSeatBitmapX + checkedSeatBitmap.getWidth() + spacing1 + txtWidth + spacing;
        tempMatrix.setScale(xScale1,yScale1);
        tempMatrix.postTranslate(waitingSeatBitmapX,(headHeight - seatHeight) / 2);
        canvas.drawBitmap(waitingSeatBitmap, tempMatrix, headPaint);
        canvas.drawText("暂离", waitingSeatBitmapX + spacing1 + seatWidth, txtY, headPaint);

        //绘制分割线
        headPaint.setStrokeWidth(1);
        headPaint.setColor(getResources().getColor(R.color.colorPrimary));
        canvas.drawLine(0, headHeight, getWidth(), headHeight, headPaint);
        return bitmap;
    }

    void drawSeat(Canvas canvas) {

        zoom = getMatrixScaleX();
        long startTime = System.currentTimeMillis();
        float translateX = getTranslateX();
        float translateY = getTranslateY();
        float scaleX = zoom;
        float scaleY = zoom;

        for (int i = 0; i < row; i++) {
            float top = i * availableSeatBitmap.getHeight() * yScale1 * scaleY + i * verSpacing * scaleY + translateY;

            float bottom = top + availableSeatBitmap.getHeight() * yScale1 * scaleY;
            if (bottom < 0 || top > getHeight()) {
                continue;
            }

            for (int j = 0; j < column; j++) {
                float left = j * availableSeatBitmap.getWidth() * xScale1 * scaleX + j * spacing * scaleX + translateX;

                float right = (left + availableSeatBitmap.getWidth() * xScale1 * scaleY);
                if (right < 0 || left > getWidth()) {
                    continue;
                }

                tempMatrix.setTranslate(left, top);
                tempMatrix.postScale(xScale1, yScale1, left, top);
                tempMatrix.postScale(scaleX, scaleY, left, top);

                int seatType = getSeatType(i, j);
                switch (seatType) {
                    case SEAT_TYPE_AVAILABLE:
                        canvas.drawBitmap(availableSeatBitmap, tempMatrix, paint);
                        break;
                    case SEAT_TYPE_NOT_AVAILABLE:
                        break;
                    case SEAT_TYPE_SELECTED:
                        canvas.drawBitmap(checkedSeatBitmap, tempMatrix, paint);
                        drawText(canvas, i, j, top, left);
                        //isSelecting = true;
                        break;
                    case SEAT_TYPE_USED:
                        canvas.drawBitmap(usedSeatBitmap, tempMatrix, paint);
                        break;
                    case SEAT_TYPE_WAITING:
                        canvas.drawBitmap(waitingSeatBitmap, tempMatrix, paint);
                        break;
                }
            }
        }

        if (DBG) {
            long drawTime = System.currentTimeMillis() - startTime;
            Log.d("drawTime", "seatDrawTime:" + drawTime);
        }
    }

    /**
     * 绘制选中座位的行号列号
     *
     * @param row 座位行号
     * @param column  座位列号
     */
    private void drawText(Canvas canvas, int row, int column, float top, float left) {

        String txt = (row + 1) + "排";
        String txt1 = (column + 1) + "座";

        if(seatChecker!=null){
            String[] strings = seatChecker.checkedSeatTxt(row, column);
            if(strings!=null&&strings.length>0){
                if(strings.length>=2){
                    txt=strings[0];
                    txt1=strings[1];
                }else {
                    txt=strings[0];
                    txt1=null;
                }
            }
        }

        TextPaint txtPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(txt_color);
        txtPaint.setTypeface(Typeface.DEFAULT_BOLD);
        float seatHeight = this.seatHeight * getMatrixScaleX();
        float seatWidth = this.seatWidth * getMatrixScaleX();
        txtPaint.setTextSize(seatHeight / 3);

        //获取中间线
        float center = seatHeight / 2;
        float txtWidth = txtPaint.measureText(txt);
        float startX = left + seatWidth / 2 - txtWidth / 2;

        //只绘制一行文字
        if(txt1==null){
            canvas.drawText(txt, startX, getBaseLine(txtPaint, top, top + seatHeight), txtPaint);
        }else {
            canvas.drawText(txt, startX, getBaseLine(txtPaint, top, top + center), txtPaint);
            canvas.drawText(txt1, startX, getBaseLine(txtPaint, top + center, top + center + seatHeight / 2), txtPaint);
        }

        if (DBG) {
            Log.d("drawTest:", "top:" + top);
        }
    }

    int bacColor = Color.parseColor("#7e000000");

    /**
     * 绘制行号
     */
    void drawNumber(Canvas canvas) {
        long startTime = System.currentTimeMillis();
        lineNumberPaint.setColor(bacColor);
        int translateY = (int) getTranslateY();
        float scaleY = getMatrixScaleY();

        rectF.top = translateY - lineNumberTxtHeight / 2;
        rectF.bottom = translateY + (seatBitmapHeight * scaleY) + lineNumberTxtHeight / 2;
        rectF.left = 0;
        rectF.right = numberWidth;
        //noinspection IntegerDivisionInFloatingPointContext
        canvas.drawRoundRect(rectF, numberWidth / 2, numberWidth / 2, lineNumberPaint);

        lineNumberPaint.setColor(Color.WHITE);

        for (int i = 0; i < row; i++) {

            float top = (i *seatHeight + i * verSpacing) * scaleY + translateY;
            float bottom = (i * seatHeight + i * verSpacing + seatHeight) * scaleY + translateY;
            float baseline = (bottom + top - lineNumberPaintFontMetrics.bottom - lineNumberPaintFontMetrics.top) / 2;

            //noinspection IntegerDivisionInFloatingPointContext
            canvas.drawText(lineNumbers.get(i), numberWidth / 2, baseline, lineNumberPaint);
        }

        if (DBG) {
            long drawTime = System.currentTimeMillis() - startTime;
            Log.d("drawTime", "drawNumberTime:" + drawTime);
        }
    }

    /**
     * 绘制概览图
     */
    void drawOverview(Canvas canvas) {

        //绘制红色框
        int left = (int) -getTranslateX();
        if (left < 0) {
            left = 0;
        }
        left /= overviewScale;
        left /= getMatrixScaleX();

        int currentWidth = (int) (getTranslateX() + (column * seatWidth + spacing * (column - 1)) * getMatrixScaleX());
        if (currentWidth > getWidth()) {
            currentWidth = currentWidth - getWidth();
        } else {
            currentWidth = 0;
        }
        int right = (int) (rectW - currentWidth / overviewScale / getMatrixScaleX());

        float top = -getTranslateY() + headHeight;
        if (top < 0) {
            top = 0;
        }
        top /= overviewScale;
        top /= getMatrixScaleY();
        if (top > 0) {
            top += overviewVerSpacing;
        }

        int currentHeight = (int) (getTranslateY() + (row * seatHeight + verSpacing * (row - 1)) * getMatrixScaleY());
        if (currentHeight > getHeight()) {
            currentHeight = currentHeight - getHeight();
        } else {
            currentHeight = 0;
        }
        int bottom = (int) (rectH - currentHeight / overviewScale / getMatrixScaleY());

        canvas.drawRect(left, top, right, bottom, redBorderPaint);
    }

    void drawOverview() {
        isDrawOverviewBitmap = false;

        int bac = Color.parseColor("#7e000000");
        overviewPaint.setColor(bac);
        overviewPaint.setAntiAlias(true);
        overviewPaint.setStyle(Paint.Style.FILL);
        overviewBitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(overviewBitmap);
        //绘制透明灰色背景
        canvas.drawRect(0, 0, rectW, rectH, overviewPaint);

        overviewPaint.setColor(Color.WHITE);
        for (int i = 0; i < row; i++) {
            float top = i * rectHeight + i * overviewVerSpacing + overviewVerSpacing;
            for (int j = 0; j < column; j++) {

                int seatType = getSeatType(i, j);
                switch (seatType) {
                    case SEAT_TYPE_AVAILABLE:
                        overviewPaint.setColor(Color.WHITE);
                        break;
                    case SEAT_TYPE_NOT_AVAILABLE:
                        continue;
                    case SEAT_TYPE_SELECTED:
                        overviewPaint.setColor(overview_checked);
                        break;
                    case SEAT_TYPE_USED:
                        overviewPaint.setColor(overview_used);
                        break;
                    case SEAT_TYPE_WAITING:
                        overviewPaint.setColor(overview_waiting);
                        break;
                }

                float left;

                left = j * rectWidth + j * overviewSpacing + overviewSpacing;
                canvas.drawRect(left, top, left + rectWidth, top + rectHeight, overviewPaint);
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 选择座位
     */
    private int selectedId;
    public int getSelectedId(){ return selectedId; }
    public void setSelectedId(int id){ selectedId = id; }

    private void ChooseSeat(int row, int column) {
        selectedId = getID(row, column);
    }

    private boolean isSeatSelected(int row, int column) {
        return (selectedId == getID(row, column));
    }

    public void cancelSelect() { selectedId = 0; }

    public int getID(int row, int column) {
        return row * this.column + (column + 1);
    }

    /*public ArrayList<String> getSelectedSeat(){
        ArrayList<String> results=new ArrayList<>();
        for(int i=0;i<this.row;i++){
            for(int j=0;j<this.column;j++){
                if(isSeatSelected(i,j)){
                    results.add(i+","+j);
                }
            }
        }
        return results;
    }*/

    /**
     * 查看选中的座位信息
     * @param row  选中座位的行号
     * @param column  选中座位的列号
     * @return  返回选中座位的状态
     */
    private int getSeatType(int row, int column) {

        if (isSeatSelected(row, column)) {
            return SEAT_TYPE_SELECTED;
        }

        if (seatChecker != null) {
            if (!seatChecker.isValidSeat(row, column)) {
                return SEAT_TYPE_NOT_AVAILABLE;
            } else if (seatChecker.isUsed(row, column)) {
                return SEAT_TYPE_USED;
            } else if (seatChecker.isWaiting(row, column)) {
                return SEAT_TYPE_WAITING;
            }
        }

        return SEAT_TYPE_AVAILABLE;
    }


    public interface SeatChecker {
        /**
         * 座位是否可用
         *
         * @param row  座位行号
         * @param column  座位列号
         * @return  判断是否可用，用来设置过道
         */
        boolean isValidSeat(int row, int column);

        /**
         * @param row  座位行号
         * @param column  座位列号
         * @return  判断座位是否被占用
         */
        boolean isUsed(int row, int column);

        boolean isWaiting(int row, int column);

        void checked(int row, int column);

        void unCheck(int row, int column);

        /**
         * 获取选中后座位上显示的文字
         * @param row  座位行号
         * @param column  座位列号
         * @return 返回2个元素的数组,第一个元素是第一行的文字,第二个元素是第二行文字,如果只返回一个元素则会绘制到座位图的中间位置
         */
        String[] checkedSeatTxt(int row,int column);

    }

    public void setData(int row, int column) {
        this.row = row;
        this.column = column;
        init();
        invalidate();
    }

    public void setSeatChecker(SeatChecker seatChecker) {
        this.seatChecker = seatChecker;
        invalidate();
    }

    private SeatChecker seatChecker;

    /*public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public void setMaxSelected(int maxSelected) {
        this.maxSelected = maxSelected;
    }*/

    /*private int getRowNumber(int row){
        int result=row;
        if(seatChecker==null){
            return -1;
        }

        for(int i=0;i<row;i++){
            for (int j=0;j<column;j++){
                if(seatChecker.isValidSeat(i,j)){
                    break;
                }

                if(j==column-1){
                    if(i==row){
                        return -1;
                    }
                    result--;
                }
            }
        }
        return result;
    }

    private int getColumnNumber(int row,int column){
        int result=column;
        if(seatChecker==null){
            return -1;
        }

        for(int i=row;i<=row;i++){
            for (int j=0;j<column;j++){

                if(!seatChecker.isValidSeat(i,j)){
                    if(j==column){
                        return -1;
                    }
                    result--;
                }
            }
        }
        return result;
    }*/



    /**
     * 自动回弹
     * 整个大小不超过控件大小的时候:
     * 往左边滑动,自动回弹到行号右边
     * 往右边滑动,自动回弹到右边
     * 往上,下滑动,自动回弹到顶部
     * <p>
     * 整个大小超过控件大小的时候:
     * 往左侧滑动,回弹到最右边,往右侧滑回弹到最左边
     * 往上滑动,回弹到底部,往下滑动回弹到顶部
     */
    private void autoScroll() {
        float currentSeatBitmapWidth = seatBitmapWidth * getMatrixScaleX();
        float currentSeatBitmapHeight = seatBitmapHeight * getMatrixScaleY();
        float moveXLength = 0;

        //处理左右滑动的情况
        if (currentSeatBitmapWidth < getWidth()) {
            if (getTranslateX() < 0 || getMatrixScaleX() < numberWidth + spacing) {
                //计算要移动的距离

                if (getTranslateX() < 0) {
                    moveXLength = (-getTranslateX()) + numberWidth + spacing;
                } else {
                    moveXLength = numberWidth + spacing - getTranslateX();
                }

            }
        } else {

            /*if (getTranslateX() < 0 && getTranslateX() + currentSeatBitmapWidth > getWidth()) {

            } else {*/
            //往左侧滑动
            if (getTranslateX() + currentSeatBitmapWidth < getWidth()) {
                moveXLength = getWidth() - (getTranslateX() + currentSeatBitmapWidth);
            } else {
                //右侧滑动
                moveXLength = -getTranslateX() + numberWidth + spacing;
            }
            //}

        }

        float startYPosition = /*screenHeight * */getMatrixScaleY() + verSpacing * getMatrixScaleY() + headHeight + borderHeight;

        //处理上下滑动
        float moveYLength;
        if (currentSeatBitmapHeight+headHeight < getHeight()) {

            if (getTranslateY() < startYPosition) {
                moveYLength = startYPosition - getTranslateY();
            } else {
                moveYLength = -(getTranslateY() - (startYPosition));
            }

        } else {

            /*if (getTranslateY() < 0 && getTranslateY() + currentSeatBitmapHeight > getHeight()) {

            } else {*/
            //往上滑动
            if (getTranslateY() + currentSeatBitmapHeight < getHeight()) {
                moveYLength = getHeight() - (getTranslateY() + currentSeatBitmapHeight);
            } else {
                moveYLength = -(getTranslateY() - (startYPosition));
            }
            //}
        }

        Point start = new Point();
        start.x = (int) getTranslateX();
        start.y = (int) getTranslateY();

        Point end = new Point();
        end.x = (int) (start.x + moveXLength);
        end.y = (int) (start.y + moveYLength);

        moveAnimate(start, end);

    }

    private void autoScale() {

        if (getMatrixScaleX() > 2.2) {
            zoomAnimate(getMatrixScaleX(), 3.0f);
        } else if (getMatrixScaleX() < 0.98) {
            zoomAnimate(getMatrixScaleX(), 2.0f);
        }
    }

    float[] m = new float[9];

    private float getTranslateX() {
        matrix.getValues(m);
        return m[2];
    }

    private float getTranslateY() {
        matrix.getValues(m);
        return m[5];
    }

    private float getMatrixScaleY() {
        matrix.getValues(m);
        return m[4];
    }

    private float getMatrixScaleX() {
        matrix.getValues(m);
        return m[Matrix.MSCALE_X];
    }

    private float getBaseLine(Paint p, float top, float bottom) {
        Paint.FontMetrics fontMetrics = p.getFontMetrics();
        int baseline;
        baseline = (int) ((bottom + top - fontMetrics.bottom - fontMetrics.top) / 2);
        return baseline;
    }

    private void moveAnimate(Point start, Point end) {
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new MoveEvaluator(), start, end);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        MoveAnimation moveAnimation = new MoveAnimation();
        valueAnimator.addUpdateListener(moveAnimation);
        valueAnimator.setDuration(400);
        valueAnimator.start();
    }

    private void zoomAnimate(float cur, float tar) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(cur, tar);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        ZoomAnimation zoomAnim = new ZoomAnimation();
        valueAnimator.addUpdateListener(zoomAnim);
        valueAnimator.addListener(zoomAnim);
        valueAnimator.setDuration(400);
        valueAnimator.start();
    }

    private float zoom;

    private void zoom(float zoom) {
        float z = zoom / getMatrixScaleX();
        matrix.postScale(z, z, scaleX, scaleY);
        invalidate();
    }

    private void move(Point p) {
        float x = p.x - getTranslateX();
        float y = p.y - getTranslateY();
        matrix.postTranslate(x, y);
        invalidate();
    }

    class MoveAnimation implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Point p = (Point) animation.getAnimatedValue();

            move(p);
        }
    }

    class MoveEvaluator implements TypeEvaluator {

        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Point startPoint = (Point) startValue;
            Point endPoint = (Point) endValue;
            int x = (int) (startPoint.x + fraction * (endPoint.x - startPoint.x));
            int y = (int) (startPoint.y + fraction * (endPoint.y - startPoint.y));
            return new Point(x, y);
        }
    }

    class ZoomAnimation implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            zoom = (Float) animation.getAnimatedValue();
            zoom(zoom);

            if (DBG) {
                Log.d("zoomTest", "zoom:" + zoom);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

    }

    ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            isScaling = true;
            float scaleFactor = detector.getScaleFactor();
            if (getMatrixScaleY() * scaleFactor > 3) {
                scaleFactor = 3 / getMatrixScaleY();
            }
            if (firstScale) {
                scaleX = detector.getCurrentSpanX();
                scaleY = detector.getCurrentSpanY();
                firstScale = false;
            }

            if (getMatrixScaleY() * scaleFactor < 0.5) {
                scaleFactor = 0.5f / getMatrixScaleY();
            }
            matrix.postScale(scaleFactor, scaleFactor, scaleX, scaleY);
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScaling = false;
            firstScale = true;
        }
    });

    GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            isOnClick = true;
            int x = (int) e.getX();
            int y = (int) e.getY();

            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    int tempX = (int) ((j * seatWidth + j * spacing) * getMatrixScaleX() + getTranslateX());
                    int maxTemX = (int) (tempX + seatWidth * getMatrixScaleX());

                    int tempY = (int) ((i * seatHeight + i * verSpacing) * getMatrixScaleY() + getTranslateY());
                    int maxTempY = (int) (tempY + seatHeight * getMatrixScaleY());

                    if (seatChecker != null && seatChecker.isValidSeat(i, j) &&
                            !seatChecker.isUsed(i, j) && !seatChecker.isWaiting(i, j)) {
                        if (x >= tempX && x <= maxTemX && y >= tempY && y <= maxTempY) {
                            if (isSeatSelected(i,j)) {
                                cancelSelect();
                                if (seatChecker != null) {
                                    seatChecker.unCheck(i, j);
                                    //isSelecting = false;
                                    ChooseSeatActivity.instance.refreshButton(false);
                                }
                            } else {
                                if (selectedId != 0) {
                                    Toast.makeText(getContext(), "只能选择1个座位", Toast.LENGTH_SHORT).show();
                                    return super.onSingleTapConfirmed(e);
                                } else {
                                    ChooseSeat(i, j);
                                    ChooseSeatActivity.instance.refreshButton(true);
                                    if (seatChecker != null) {
                                        seatChecker.checked(i, j);
                                    }
                                }
                            }
                            isNeedDrawSeatBitmap = true;
                            isDrawOverviewBitmap = true;
                            float currentScaleY = getMatrixScaleY();

                            if (currentScaleY < 1.7) {
                                scaleX = x;
                                scaleY = y;
                                zoomAnimate(currentScaleY, 1.9f);
                            }
                            performClick();
                            invalidate();
                            break;
                        }
                    }
                }
            }

            return super.onSingleTapConfirmed(e);
        }
    });
}
