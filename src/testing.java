import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.ParseException;
public class testing {

	public static void main(String[] args) throws ParseException {
			Jep jep = new Jep();
			Object res = null;
			try {
				jep.parse("10-(1-3)*7");
				res = jep.evaluate();
			} catch (EvaluationException e) {
			}
			Double ca = (Double) res;
			
			System.out.println(ca ==24);

	}
}
