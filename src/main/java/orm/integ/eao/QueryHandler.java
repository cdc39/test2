package orm.integ.eao;

import java.util.List;

import orm.integ.dao.DataAccessObject;
import orm.integ.dao.sql.QueryRequest;
import orm.integ.eao.model.Entity;
import orm.integ.eao.model.EntityModel;
import orm.integ.eao.model.PageData;
import orm.integ.eao.model.Record;

public abstract class QueryHandler {

	@SuppressWarnings("rawtypes")
	EntityAccessObject eao ;
	DataAccessObject dao;
	EntityModel model;
	RecordExtender filler;
	QueryRequest query;
	
	@SuppressWarnings("rawtypes")
	QueryHandler(EntityAccessObject eao) {
		this.eao = eao;
		dao = eao.getDAO();
		model = eao.getEntityModel();
	}
	
	protected void setQueryRequest(QueryRequest query) {
		this.query = query;
	}

	
	public QueryHandler setPageInfo(int start, int limit) {
		query.setPageInfo(start, limit);
		return this;
	}
	
	public QueryHandler setFields(String... fields) {
		query.setViewFields(fields);
		return this;
	}
	
	public QueryHandler setRecordFiller(RecordExtender filler) {
		this.filler = filler;
		return this;
	}


	@SuppressWarnings("unchecked")
	public <E extends Entity> E first() {
		return (E) eao.queryFirst(query);
	}
	
	public int count() {
		return eao.queryCount(query); 
	}
	
	@SuppressWarnings("rawtypes")
	public List list() {
		return eao.query(query);
	}
	
	public PageData page() {
		PageData page = eao.pageQuery(query);
		fillExtendValues(page.getList());
		return page;
	}
	
	protected void fillExtendValues(List<Record> recs) {
		if (filler!=null) {
			for (Record r: recs) {
				filler.fillRecordExt(r);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Record> recordList(){
		List list = list();
		List<Record> recList = eao.toRecords(list);
		fillExtendValues(recList);
		return recList;
	}
	

	
}
