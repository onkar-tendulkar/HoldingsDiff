import java.io.IOException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;


public class HoldingsDiffMain
{
	private static SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
	private static SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
	
	private static JdbcTemplate jdbcTemplate = null;

	public static void main(String[] args)
	{
		
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("Beans.xml");
		
		
		jdbcTemplate = (JdbcTemplate) context.getBean("jdbcTemplate");
		
		parseFund("23010");
		parseFund("23414");
		parseFund("23960");
		parseFund("24634");
		parseFund("6821");
		parseFund("13853");
		parseFund("10599");
		parseFund("22504");
		parseFund("10563");
		parseFund("5882");
		parseFund("6669");
		parseFund("2222");
		parseFund("3161");
		parseFund("23498");
	}
	
	public static void parseFund(String schemeCode)
	{
		Document doc;
		try
		{
			//https://www.valueresearchonline.com/funds/newsnapshot.asp?schemecode=23414
			doc = Jsoup.connect("https://www.valueresearchonline.com/funds/newsnapshot.asp?schemecode="+schemeCode).timeout(0).get();
			Elements newsHeadlines = doc.select("th:contains(Top Holdings)");
			
			Element tableBody = newsHeadlines.first().parent().parent();
			
			Elements rows = tableBody.select("tr");
			
			Element dateHTML = tableBody.parent().parent().select("div.footnote").last();
			Date asOfDate=null;
			try
			{
				asOfDate = formatter.parse(dateHTML.text().replaceFirst("As on ", ""));
			} catch (ParseException e)
			{
				if(asOfDate == null)
				{
					asOfDate = new Date();
				}
			}
			
			for (Element element : rows)
			{
				Elements columns = element.select("td");
				if(columns.size()>0)
				{
					Element company = columns.get(1);
					Element percentageHoldings = columns.get(6);
					System.out.println(schemeCode + "|" + asOfDate + "|" +company.text() + "|" + percentageHoldings.text());
					//String sql = "CALL holdingdiffdb.pcu_holding("+schemeCode+",'"+company.text()+"',"+percentageHoldings.text()+",'"+formatter2.format(asOfDate)+"');";
					String sql = "CALL holdingdiffdb.pcu_holding(?,?,?,?);";
					System.out.println(sql);
					
					Object[] params = { schemeCode, company.text(), percentageHoldings.text(),formatter2.format(asOfDate)};
					int[] types = {Types.VARCHAR, Types.VARCHAR, Types.DECIMAL, Types.DATE};
					jdbcTemplate.update(sql, params, types);
				}
			}
			System.out.println("Completed processing : "+schemeCode);
			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
