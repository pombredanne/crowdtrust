package crowdtrust;

public class MultiValueR extends Response{
	
	int selection;

	@Override
	Byte[] serialise() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object o) {
		MultiValueR mv = (MultiValueR) o;
		return selection == mv.selection;
	}

}