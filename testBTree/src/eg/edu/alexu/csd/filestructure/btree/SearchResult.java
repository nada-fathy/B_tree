package eg.edu.alexu.csd.filestructure.btree;

public class SearchResult implements ISearchResult{
	
	private String ID;
	private int Rank;

	public SearchResult(String id, int rank) {
		ID=id;
		Rank=rank;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void setId(String id) {
		ID=id;
		
	}

	@Override
	public int getRank() {
		return Rank;
	}

	@Override
	public void setRank(int rank) {
		Rank=rank;
		
	}

}
