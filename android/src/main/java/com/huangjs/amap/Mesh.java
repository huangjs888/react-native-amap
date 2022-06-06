package com.huangjs.amap;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.UUID;

public class Mesh {
  // 唯一ID
  private String id = null;
  // 着色器
  private Shader shader = null;
  // 是否前后面都渲染
  private String backOrFront = "both";
  // 是否开启透明度效果
  private boolean transparent = true;
  // 顶点缓存
  private FloatBuffer vertexBuffer = null;
  // 颜色缓存
  private FloatBuffer colorBuffer = null;
  // 顶点序号（三个点构造一个三角形面）
  private IntBuffer faceBuffer = null;
  // 点分量（x,y,z）个数*3
  private int vertexLength = 0;
  // 序号个数
  private int faceLength = 0;
  // 变换矩阵
  private final float[] mvpMatrix = new float[16];
    /*// 用户设置矩阵
    private final float[] initMatrix = new float[16];*/

  public Mesh() {
    this("both", true);
  }

  public Mesh(String backOrFront, boolean transparent) {
    this.id = UUID.randomUUID().toString();
    this.backOrFront = backOrFront;
    this.transparent = transparent;
    Matrix.setIdentityM(mvpMatrix, 0);
    // Matrix.setIdentityM(initMatrix, 0);
  }

  public String getId() {
    return id;
  }

  public void updateViewport(int x, int y, int width, int height) {
    GLES20.glViewport(x, y, width, height);
  }

  public boolean isInitShader() {
    return shader != null;
  }

  public void initShader() {
    shader = new Shader();
    shader.create();
  }

  public void destroy() {
    vertexBuffer = null;
    colorBuffer = null;
    faceBuffer = null;
    // 可能出现mesh对象已创建，但是因为地图没加载完成导致没有isInitShader，此时销毁mesh时shader为null
    if (shader != null) {
      shader.destroy();
    }
    shader = null;
  }

  public void setData(float[] vertices, float[] vertexColors, int[] faces) {
    // 顶点buffer
    vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)// 直接分配一个4倍于vertices长度的byte buffer
      .order(ByteOrder.nativeOrder()) // 设置byte buffer的字节序和本地jvm运行的硬件的字节顺序一致，ByteOrder.nativeOrder()返回本地jvm运行的硬件的字节顺序，使用和硬件一致的字节顺序可能使buffer更加有效率。
      .asFloatBuffer();// 创建此byte buffer的视图为float buffer
    vertexBuffer.clear();
    vertexBuffer.put(vertices).position(0);
    this.vertexLength = vertices.length;
    // 颜色buffer
    colorBuffer = ByteBuffer.allocateDirect(vertexColors.length * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer();
    colorBuffer.clear();
    colorBuffer.put(vertexColors).position(0);
    // 面（定点序号）buffer
    faceBuffer = ByteBuffer.allocateDirect(faces.length * 4)
      .order(ByteOrder.nativeOrder())
      .asIntBuffer();// 创建此byte buffer的视图为int buffer
    faceBuffer.clear();
    faceBuffer.put(faces).position(0);
    this.faceLength = faces.length;
  }

  public void setBackOrFront(int bof) {
    // 设置两面绘制，还是前面或后面
    this.backOrFront = bof == 1 ? "front" : bof == 2 ? "back" : "both";
    drawGl();
  }

  public void transparentEnabled(boolean enabled) {
    // 是否启用透明度分量
    this.transparent = enabled;
    drawGl();
  }

    /*public void rotate(float angle, float[] axis) {
        // 对当前图形矩阵旋转,angle角度，axis代表xyz坐标与原点形成的旋转轴
        if (axis == null || axis.length < 3) {
            axis = new float[]{0.0f, 0.0f, 1.0f};
        }
        Matrix.rotateM(initMatrix, 0, angle, axis[0], axis[1], axis[2]);
        drawGl();
    }

    public void scale(float scale) {
        // 对当前图形矩阵缩放,scale代表缩放比例，默认xyz缩放比例一样，不做分量
        Matrix.scaleM(initMatrix, 0, scale, scale, scale);
        drawGl();
    }

    public void translate(float x, float y, float z) {
        // 对当前图形分别沿着xyz移动
        Matrix.translateM(initMatrix, 0, x, y, z);
        drawGl();
    }

    public void transform(float[] matrix) {
        // 对当前图形直接使用矩阵变换
        if (matrix != null) {
            Matrix.multiplyMM(initMatrix, 0, initMatrix, 0, matrix, 0);
        }
        drawGl();
    }*/

  public void draw(float[] matrix, float[] translate, float[] scale, float[] rotate) {
    // 对当前图形重新绘图
    Matrix.setIdentityM(mvpMatrix, 0);
    if (matrix != null) {
      Matrix.multiplyMM(mvpMatrix, 0, /*initMatrix*/ mvpMatrix, 0, matrix, 0);
    }
    if (translate != null && translate.length >= 3) {
      Matrix.translateM(mvpMatrix, 0, translate[0], translate[1], translate[2]);
    }
    if (scale != null && scale.length >= 3) {
      Matrix.scaleM(mvpMatrix, 0, scale[0], scale[1], scale[2]);
    }
    if (rotate != null && rotate.length >= 3) {
      Matrix.rotateM(mvpMatrix, 0, rotate[3], rotate[0], rotate[1], rotate[2]);
    }
    drawGl();
  }

  private void drawGl() {
    if (null == shader) return;
    // 使用program
    GLES20.glUseProgram(shader.program);
    // 开启深度测试
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    // 开启或关闭面剔除
    if (backOrFront.equals("both")) {
      GLES20.glDisable(GLES20.GL_CULL_FACE);
    } else {
      GLES20.glEnable(GLES20.GL_CULL_FACE);
      if (backOrFront.equals("front")) {
        GLES20.glCullFace(GLES20.GL_BACK);
      } else if (backOrFront.equals("back")) {
        GLES20.glCullFace(GLES20.GL_FRONT);
      } else {
        GLES20.glCullFace(GLES20.GL_FRONT_AND_BACK);
      }
    }
    // 开启可设置透明度效果
    if (transparent) {
      // 指定alpha分量的像素算术
      GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
      GLES20.glEnable(GLES20.GL_BLEND);
    } else {
      GLES20.glDisable(GLES20.GL_BLEND);
    }
    // 顶点指针
    int vertexSize = 3;
    GLES20.glVertexAttribPointer(shader.aVertex, vertexSize, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(shader.aVertex);
    // 颜色指针
    int colorSize = 4;
    GLES20.glVertexAttribPointer(shader.aColor, colorSize, GLES20.GL_FLOAT, false, 0, colorBuffer);
    GLES20.glEnableVertexAttribArray(shader.aColor);
    // 位置矩阵
    GLES20.glUniformMatrix4fv(shader.uMVPMatrix, 1, false, mvpMatrix, 0);
    // 开始画
    if (faceLength != 0) {
      GLES20.glDrawElements(GLES20.GL_TRIANGLES, faceLength, GLES20.GL_UNSIGNED_INT, faceBuffer);
    } else {
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexLength / vertexSize);
    }
    // 禁掉
    GLES20.glDisableVertexAttribArray(shader.aVertex);
    GLES20.glDisableVertexAttribArray(shader.aColor);
    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    GLES20.glDisable(GLES20.GL_CULL_FACE);
    GLES20.glDisable(GLES20.GL_BLEND);
  }

  private static class Shader {
    int aVertex;
    int aColor;
    int uMVPMatrix;
    int program;

    String vertexShaderSource = "   // 顶点着色器，定义点和颜色\n\n" +
      "   precision mediump float;// 定义所有浮点类型都是中精度\n\n" +
      "   attribute vec3 a_vertex;// attribute声明vec3类型位置变量a_vertex\n" +
      "   attribute vec4 a_color;// attribute声明vec4类型颜色变量a_color\n\n" +
      "   uniform mat4 u_MVPMatrix;// uniform声明mat4类型矩阵变量u_MVPMatrix\n\n" +
      "   varying vec4 v_color;//\n\n" +
      "   void main(){\n" +
      "       v_color = a_color;\n" +
      "       gl_Position = u_MVPMatrix * vec4(a_vertex, 1.0);\n" +
      "   }";

    String fragmentShaderSource = "   // 片元着色器，只做了颜色处理，没有纹理处理\n\n" +
      "   precision mediump float;// 定义所有浮点类型都是中精度\n\n" +
      "   varying vec4 v_color;\n\n" +
      "   void main(){\n" +
      "       gl_FragColor = v_color;\n" +
      "   }";

    public void create() {
      // 创建顶点、片元着色器对象
      int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
      int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
      //引入顶点、片元着色器源代码
      GLES20.glShaderSource(vertexShader, vertexShaderSource);
      GLES20.glShaderSource(fragmentShader, fragmentShaderSource);
      //编译顶点、片元着色器
      GLES20.glCompileShader(vertexShader);
      GLES20.glCompileShader(fragmentShader);
      // 创建程序对象program
      program = GLES20.glCreateProgram();
      // 附着顶点着色器和片元着色器到program
      GLES20.glAttachShader(program, vertexShader);
      GLES20.glAttachShader(program, fragmentShader);
      // 链接program
      GLES20.glLinkProgram(program);
      // 获取顶点着色器的位置变量a_vertex，颜色变量a_color，即aVertex指向a_vertex变量，aColor指向a_color变量
      aVertex = GLES20.glGetAttribLocation(program, "a_vertex");
      aColor = GLES20.glGetAttribLocation(program, "a_color");
      uMVPMatrix = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
    }

    public void destroy() {
      GLES20.glDeleteShader(program);
      GLES20.glDeleteProgram(program);
    }
  }
}

