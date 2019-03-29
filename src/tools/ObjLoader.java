package tools;

import java.awt.*;
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import java.io.*;
import java.net.URL;
import com.sun.j3d.loaders.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import com.sun.j3d.utils.image.TextureLoader;

// �� Loader Object Class
public class ObjLoader extends LoaderBase {   
	SceneBase       Base;      // SceneBase
    // OBJ ���͊֌W
    BufferedReader  BR;        // ���� Reader
    String          BUF;       // �P�s���̓��̓o�b�t�@
    StringTokenizer token;     // �g�[�N�� Object
    // material �֌W
    BufferedReader  mtlBR;     // ���� Reader
    String          mtlBUF;    // �P�s���̓��̓o�b�t�@
    String          mtlID;     // usemtl ID
    String          mtlTEX;    // Texture name

    ArrayList<Point3f>    vp = new ArrayList<Point3f>();    // ���_���W
    ArrayList<TexCoord2f> vt = new ArrayList<TexCoord2f>(); // �e�N�X�`�����W
    ArrayList<Vector3f>   vn = new ArrayList<Vector3f>();   // �@���x�N�g��

    ArrayList<Integer>    idx = new ArrayList<Integer>();   // Index �̋�؂�
    ArrayList<Integer>    xvp = new ArrayList<Integer>();   // ���_ Index �̕���
    ArrayList<Integer>    xvt = new ArrayList<Integer>();   // �e�N�X�`�� Index �̕���
    ArrayList<Integer>    xvn = new ArrayList<Integer>();   // �@���x�N�g�� Index �̕���
    
    List<int[]> indexes = new ArrayList<int[]>();
    
    static String filePath = "";

    public Scene load(String fname) {
    	System.out.println(fname.length());
    	
    	if (fname.substring(0, 6).equals("file:/")) {
    		fname = fname.substring(6, fname.length());
    	}
    	
    	for (int i = fname.length(); i > 0; i--) {
    		if (fname.substring(i - 1, i).equals("\\") || fname.substring(i - 1, i).equals("/")) {
        		filePath = fname.substring(0, i);
        		break;
    		}
    	}
    	
        File    file = new File(fname);
        try
        {   BR = new BufferedReader(new FileReader(file));  }
        catch(IOException e)
        {   System.out.println("File Open Error" + fname);  }

        Base = new SceneBase();
        Base.setSceneGroup(new BranchGroup());
        set_obj();          //�� OBJ data �̓o�^
        Close();
        return Base;
    }

    public Scene load(URL aURL) {
    	Base = new SceneBase();
        return Base;
    }

    public Scene load(Reader reader) {
    	Base = new SceneBase();
        return Base;
    }

    // OBJ File ����͂��� Base �𐶐�
    public void set_obj() {   
    	int     i,num;
        String  str;
        int[]   val= new int[3];

        while(NextRead()) {
            str = token.nextToken();
            if ("v".equals(str)) {// ���_���W  
            	vp.add(new Point3f(FVal(), FVal(), FVal()));
            }
            else  if ("vt".equals(str)) {    // �e�N�X�`�����W
            	vt.add(new TexCoord2f(FVal(), FVal()));
            }
            else  if ("vn".equals(str)) {    // �@���x�N�g��
        		vn.add(new Vector3f(FVal(), FVal(), FVal()));
            }
            else  if ("f".equals(str)) {      // Index
                num= token.countTokens();
                idx.add(num);
                for(i=0; i<num; i++) {
                	str= token.nextToken(); // 1//2, -1/-1/-1
                    setv(str, val);
                    if (val[0]>0)       xvp.add(val[0]);
                    else  if (val[0]<0) xvp.add(vp.size()+val[0]+1);
                    if (val[1]>0)       xvt.add(val[1]);
                    else  if (val[1]<0) xvt.add(vt.size()+val[1]+1);
                    if (val[2]>0)       xvn.add(val[2]);
                    else  if (val[2]<0) xvn.add(vn.size()+val[2]+1);
                }
            }
            else  if ("mtllib".equals(str)) {// mtllib material.mtl
            	str = token.nextToken();
                open_mtl(str);
            }
            else  if ("usemtl".equals(str)) {// usemtl emerald
        		str = token.nextToken();    // ���� material ID
                creatscene();               // �|���S�����f���̐���
                idx.clear();                // Index �z��̃N���A
                xvp.clear();
                xvt.clear();
                xvn.clear();
                mtlID = str;
            }
        }
        creatscene();   // �|���S�����f���̐���
    }

    // Scene �𐶐�
    public void creatscene() {   
    	int i,cnt;

        if (xvp.size()<1) {
        	return;
        }
        
        // ArrayList ��z��ɕϊ�
        Point3f[] vertices = (Point3f[])vp.toArray(new Point3f[0]);
        TexCoord2f[] texture = (TexCoord2f[])vt.toArray(new TexCoord2f[0]);
        Vector3f[] normal = (Vector3f[])vn.toArray(new Vector3f[0]);

        // Index ����z��ɕϊ�
        int[] stripVertexCounts= new int[idx.size()];
        for (i = 0; i < idx.size(); i++) {
        	stripVertexCounts[i] = idx.get(i);
        }
        int[] indices = new int[xvp.size()];
        for (i = 0; i < xvp.size(); i++) {
        	indices[i] = xvp.get(i) - 1;
        }
        int[] texidx = new int[xvt.size()];
        for (i = 0; i < xvt.size(); i++) {
        	texidx[i] = xvt.get(i) - 1;
        }
        int[] normidx = new int[xvn.size()];
        for (i = 0; i < xvn.size(); i++) {
        	normidx[i] = xvn.get(i)-1;
        }

        // ���f���𐶐�
        GeometryInfo ginfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        ginfo.setCoordinates(vertices);
        ginfo.setCoordinateIndices(indices);
        ginfo.setStripCounts(stripVertexCounts);

        // Texture �̐ݒ�
        if (xvt.size() > 0) {
        	ginfo.setTextureCoordinateParams(1,2);
            ginfo.setTextureCoordinates(0,texture);
            ginfo.setTextureCoordinateIndices(0,texidx);
        }

        // �@���x�N�g���̐ݒ�
        if (xvn.size() > 0) {
        	ginfo.setNormals(normal);
            ginfo.setNormalIndices(normidx);
        }
        else {
        	NormalGenerator gen = new NormalGenerator();
			gen.generateNormals(ginfo);
        }

        // Material ��ݒ肵�� SceneBase �ɒǉ�
        Shape3D shape = new Shape3D(ginfo.getGeometryArray());
        shape.setAppearance(createAppearance());
        Base.getSceneGroup().addChild(shape);
        
        for (i = 0; i < indices.length / 3; i++ ) {
        	int[] integer = new int[3];
        	for (int j = 0; j < 3; j++) {
        		integer[j] = indices[i * 3 + j];
        	}
        	indexes.add(integer);
        }
    }

    // Material �̐ݒ�
    private Appearance createAppearance()
    {   Appearance ap = new Appearance();
        Material mat = new Material();

        if (mtlBR != null) {
        	if (search()) {                      // mtl ID ������
    			set_mat(mat);                   // mtl ID �̃}�e���A����ݒ�
                if (xvt.size() > 0 && mtlTEX != null) {
                    BufferedImage bimage = loadImage(mtlTEX);
                    TextureLoader texload = new TextureLoader(bimage);
                    Texture2D texture2d = (Texture2D)texload.getTexture();
                    ap.setTexture(texture2d);
                }
            }
        }
        ap.setMaterial(mat);
        
        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        ap.setTextureAttributes(texAttr);
        
        return ap;
    }

    // material File Open
    private void open_mtl(String str) {
        File    mfile = new File(filePath + str);

        try {
        	mtlBR = new BufferedReader(new FileReader(mfile));
            mtlBR.mark(2000);
        }
        catch(IOException e) {   
        	e.printStackTrace();
        	System.out.println("material Open Error= " + str);  
        }
    }

    // search mtl ID
    private boolean search() {
    	String  str;
        try {
        	mtlBR.reset();
            while ((mtlBUF = mtlBR.readLine()) != null) {
            	token = new StringTokenizer(mtlBUF, " ,\t", false);
                if (token.hasMoreTokens() == false) {
                	continue;
                }
                str = token.nextToken();
                if ("newmtl".equals(str) == false) {
                	continue;
                }
                str = token.nextToken();
                if (mtlID.equals(str)) {
                	return true;
                }
            }
        }
        catch(IOException e)
        {   System.out.println("material Read Error");  }
        return false;
    }

    // �}�e���A����ݒ�
    private void set_mat(Material mat)
    {   String  str;
        mtlTEX = null;
        try
        {   while(true)
            {   mtlBUF= mtlBR.readLine();
                if (mtlBUF==null)  return;
                token = new StringTokenizer(mtlBUF, " ,\t", false);
                if (token.hasMoreTokens()==false)   continue;
                str = token.nextToken();
                if ("newmtl".equals(str))   return;
                else  if ("Ka".equals(str))       // Ambient color
                {   mat.setAmbientColor(new Color3f(MVal(), MVal(), MVal()));
                }
                else  if ("Kd".equals(str))       // Diffuse color
                {   mat.setDiffuseColor(new Color3f(MVal(), MVal(), MVal()));
                }
                else  if ("Ks".equals(str))       // Specular
                {   mat.setSpecularColor(new Color3f(MVal(), MVal(), MVal()));
                }
                else  if ("Ns".equals(str))       // Shininess (clamped to 1.0 - 128.0)
                {   mat.setShininess(MVal());
                }
                else  if ("map_Kd".equals(str))   // map_Kd File
                {   mtlTEX = token.nextToken();
                }
            }
        }
        catch(IOException e)
        {   System.out.println("material Read Error");  }
    }

    // token(mtlBUF)���玟�̒l���擾
    private float MVal()
    {   if (token.hasMoreTokens()==false)   return 0.0f;
        return Float.parseFloat(token.nextToken());
    }

    // Line Read(BUF �ɓ���)
    private boolean LineRead()
    {
        try
        {   BUF = BR.readLine();  }
        catch(IOException e)
        {   System.out.println(e);  }
        if (BUF == null)
        {   System.out.println("End of file");
            return false;
        }
        return true;
    }

    // Next Read Line(#���X�L�b�v, Token ��ݒ�)
    private boolean NextRead()
    {
        while(LineRead())
        {   if (BUF.length()>0 && BUF.charAt(0)!='#')
            {   token = new StringTokenizer(BUF, " ,;\t", false);
                return true;
            }
        }
        return false;
    }

    // Next FVal(BUF ���玟�̒l���擾)
    private float FVal()
    {   if (token.hasMoreTokens()==false)
            if (NextRead()==false)  return -1.0f;
        return Float.parseFloat(token.nextToken());
    }

    // '/' �ŋ�؂�ꂽ�l��؂�o��("1//2")
    private void setv(String str, int[] v)
    {   int p,q,i;

        for(i=0; i<3; i++)  v[i]= 0;
        p= 0;
        for(i=0; i<3; i++)
        {   for(q=p; q<str.length() && str.charAt(q)!='/'; q++);
            if (q>p)    v[i]= Integer.parseInt(str.substring(p,q));
            p= q+1;
            if (p>=str.length())    return;
        }
    }

    // BufferedReader Close
    public void Close()
    {   try
        {   BR.close();  }
        catch(IOException e)
        {   System.out.println("File Close Error");  }
    }

    // BufferedImage �̓���
    public static BufferedImage loadImage(String fileName) {
    	fileName = filePath + fileName;
    	InputStream is = null;
        try
        {   is = new FileInputStream(fileName);
            BufferedImage img = ImageIO.read(is);
            return img;
        }
        catch (IOException e)
        {   throw new RuntimeException(e);  }
        finally
        {   if (is != null)
                try { is.close(); }
                catch (IOException e) {}
        }
    }
    
    public float[] getVertexes() {
    	float[] vertexes = new float[(vp.size()) * 3];
    	int i = 0;
    	
    	for (Point3f v: vp) {
    		vertexes[i] = v.x;
    		i++;
    		vertexes[i] = v.y;
    		i++;
    		vertexes[i] = v.z;
    		i++;
    	}
    	
    	return vertexes;
    }
    
    public List<Vector3f> getVertexPositions() {
    	List<Vector3f> vertexes = new ArrayList<Vector3f>();
    	for (Point3f v: vp) {
    		Vector3f vertex = new Vector3f(v.x, v.y, v.z);
    		vertexes.add(vertex);
    	}
    	
    	return vertexes;
    }
    
    public float[] getVerticles() {
    	float[] verticles = new float[(vp.size()) * 3];
    	int i = 0;
    	for (Point3f v: vp) {
    		verticles[i] = v.x;
    		i++;
    		verticles[i] = v.y;
    		i++;
    		verticles[i] = v.z;
    		i++;
    	}
    	return verticles;
    }
    
    public List<int[]> getIndexes() {
        return indexes;
    }
}