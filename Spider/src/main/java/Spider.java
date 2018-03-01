/**
 * @author bero-
 *
 */

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.*;
import com.mysql.jdbc.Statement;


class record{					//一条记录的类
	
	private String aid;				//视频ID
	private String author;			//视频作者
	private int favorites;			//视频收藏数
	private String category;		//视频所属目录
	private int coin;				//视频硬币数
	
	public void setaid(String aid)				//set方法
	{
		this.aid=aid;
	}
	public void setauthor(String author)			
	{
		this.author=author;
	}
	public void setfavorite(int favorites)			
	{
		this.favorites=favorites;
	}
	public void setcategory(String category)			
	{
		this.category=category;
	}
	public void setcoin(int coin)					
	{
		this.coin=coin;
	}
	public String getaid()					//get方法
	{
		return this.aid;
	}
	public String getauthor()
	{
		return this.author;
	}
	public int getfavorites()
	{
		return this.favorites;
	}
	public String getcatagory()
	{
		return this.category;
	}
	public int getcoin()
	{
		return this.coin;
	}
}

public class Spider{				//爬虫类
	
	//存储每个类别top3的ID
	private static String []top3;
	//驱动程序名
	private static String driver="com.mysql.jdbc.Driver";
	//URL指向要访问的数据库名myav
	private static String url="jdbc:mysql://localhost:3306/myav";	
	//Mysql配置时的用户名
	private static String user="root";
	//Mysql配置时的密码
	private static String password="1819";
	
	//连接数据库的函数
	public static void InsertToMySQL(record rc){		
		
		try{
			//加载驱动程序
			Class.forName(driver);
			//连接数据库
			Connection conn=DriverManager.getConnection(url,user,password);
			//要执行的sql语句
			String sql="INSERT INTO myav (avNum,authors,categories,coinsNum,favNum) values (?,?,?,?,?)";
			//建立PreparedStatement语句
			PreparedStatement prestm=conn.prepareStatement(sql);
			
			prestm.setString(1, rc.getaid());
			prestm.setString(2, rc.getauthor());
			prestm.setString(3, rc.getcatagory());
			prestm.setInt(4, rc.getcoin());
			prestm.setInt(5, rc.getfavorites());
			prestm.executeUpdate();
			
			prestm.close();
			conn.close();
		}catch(ClassNotFoundException e)
		{
			System.out.println("Sorry,can't find the driver.");
			e.printStackTrace();
		}catch(SQLException e)
		{
			e.printStackTrace();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void getDondow(String url,String pathName)throws Exception{			//下载视频的函数
		
		//建立URL
	    URL ul = new URL(url);
	    //连接URL
	    HttpURLConnection conn = (HttpURLConnection) ul.openConnection();
	    //读入
	    BufferedInputStream bi = new BufferedInputStream(conn.getInputStream());
	    
	    //写到文件中
	    FileOutputStream bs = new FileOutputStream(pathName);
	    byte[] by = new byte[1024];
	    int len = 0;
	    while((len=bi.read(by))!=-1){
	        bs.write(by,0,len);
	    }
	    bs.close();
	    bi.close();
	}
	public static void sort()
	{
		try{
			//加载驱动程序
			Class.forName(driver);
			//连接数据库
			Connection conn=DriverManager.getConnection(url,user,password);
			//建立Statement语句
			Statement stm=(Statement) conn.createStatement();
			//要执行的sql语句
			String sql="select * from myav ORDER BY favNum desc";
			//创建结果集
			ResultSet rs=stm.executeQuery(sql);
			top3=new String[3];
			for(int i=0;rs.next()&&i<3;i++)
				top3[i]=rs.getString(1);
			
			rs.close();
			conn.close();
		}catch(ClassNotFoundException e)
		{
			System.out.println("Sorry,can't find the driver.");
			e.printStackTrace();
		}catch(SQLException e)
		{
			e.printStackTrace();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void DBclear()
	{
		try{
			//加载驱动程序
			Class.forName(driver);
			//连接数据库
			Connection conn=DriverManager.getConnection(url,user,password);
		
			//要执行的sql语句
			String sql="delete from myav";
			//创建Preparedstatement语句
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.executeUpdate();
			
			conn.close();
		}catch(ClassNotFoundException e)
		{
			System.out.println("Sorry,can't find the driver.");
			e.printStackTrace();
		}catch(SQLException e)
		{
			e.printStackTrace();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws Exception
	{
			int i,j,k,num=0;
			//要爬的类别对应的tid
			int []sometid={17,20,24,25,26,27,28,30,31,32,33,59,126,158,159,164};
			
			for( k=0;k<sometid.length;k++)
			{
				for( j=1;;j++){
					try{
						//创建一个HttpClient
						HttpClient httpClient=HttpClients.createDefault();
						HttpGet req=new HttpGet("http://api.bilibili.com/archive_rank/getarchiverankbypartion?callback=?&type=jsonp&tid="+sometid[k]+"&pn="+j);
					 	req.addHeader("Accept","application/json,text/javascript,*/*;q=0.01");
						req.addHeader("Accept-Encoding", "gzip,deflate,sdch");
						req.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
						req.addHeader("Content-Type", "text/html,charset=UTF-8");
						req.addHeader("User-Agent", "Mozilla/5.0(Windows NT 5.2)AppleWebKit/537.36(KHTML,Like Gecko)Chrome/32.0.1700.76 Safari 8.0.8");
						HttpResponse rep=httpClient.execute(req);
						HttpEntity repEntity=rep.getEntity();
						String content=EntityUtils.toString(repEntity);
						//用JSON解析获取的数据
						JSONObject obj =new JSONObject(content);
						obj.getJSONObject("data").getJSONObject("archives");
						num=obj.getJSONObject("data").getJSONObject("archives").length();
						for(i=0;i<num;i++)
						{
							JSONObject myobj=obj.getJSONObject("data").getJSONObject("archives").getJSONObject(""+i);
							
							record rc=new record();
							rc.setaid(myobj.get("aid").toString());
							rc.setauthor(myobj.get("author").toString());
							rc.setfavorite(myobj.getInt("favorites"));
							rc.setcategory(myobj.get("tname").toString());
							rc.setcoin(myobj.getJSONObject("stat").getInt("coin"));
							//将一条信息插入数据库
							InsertToMySQL(rc);
							//定时休眠程序以防被拉黑不能继续爬虫0.0
							if(j*(k+1)*num%500==0)Thread.sleep(2000);
							
							System.out.print(myobj.get("tname").toString()+"类收藏数的top3的AVID为:");
						}
					}catch(Exception e)
					{
						break;
					}
			}
				//对数据库数据排序
				sort();		
				
				for(int m=0;m<3;m++){
				System.out.print(top3[m]+"\t");
				getDondow("http://www.bilibilijj.com/Files/DownLoad/"+top3[m]+".mp4/www.bilibilijj.com.mp4?mp3=true","Avideo"+k+"."+m+".mp4");
				}
				System.out.println();
				//清除数据库数据
				DBclear();
				//如果爬取的条数超过50万条，则结束程序
				if(j*(k+1)*num>=50000)break;
		}
	}
}