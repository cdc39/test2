package orm.integ.eao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import orm.integ.dao.DataAccessObject;
import orm.integ.dao.sql.StatementAndValue;
import orm.integ.dao.sql.TabQuery;
import orm.integ.eao.model.Entity;
import orm.integ.eao.model.FieldInfo;
import orm.integ.eao.model.FromOrmHelper;
import orm.integ.eao.model.Record;
import orm.integ.eao.model.Relation;
import orm.integ.eao.model.RelationModel;
import orm.integ.eao.model.TableModels;
import orm.integ.utils.IntegError;

public class RelationAccessObject extends TableHandler {

	public RelationAccessObject(DataAccessObject dao) {
		super(dao);
	}
	
	private TabQuery buildQuery(Relation rel) {
		RelationModel relModel = getModel(rel);
		FieldInfo[] keyFields = relModel.getKeyFields();
		FieldInfo key1 = keyFields[0];
		FieldInfo key2 = keyFields[1];
		Object id1 = keyFields[0].getValue(rel);
		Object id2 = keyFields[1].getValue(rel);
		String where = "";
		Object[] ids = new Object[0];
		if (id1==null && id2!=null) {
			where = key2.getColumnName()+"=?";
			ids = new Object[]{id2};
		}
		else if (id1!=null && id2==null) {
			where = key1.getColumnName()+"=?";
			ids = new Object[]{id1};
		}
		else if (id1!=null && id2!=null) {
			where = key1.getColumnName()+"=? and "+key2.getColumnName()+"=?";
			ids = new Object[]{id1, id2};
		}
		TabQuery query = new TabQuery(relModel);
		query.addWhereItem(where, ids);
		return query;
	}
	
	public <R extends Relation> R querySingle(R rel) {
		List<R> list = query(rel);
		return list.size()==1?list.get(0):null;
	}
	
	public boolean relationExists(Relation rel) {
		TabQuery query = buildQuery(rel);
		int count = dao.queryCount(query);
		return count>0;
	}
	
	public boolean insert(Relation rel){
		if (rel==null) {
			return false;
		}
		if (relationExists(rel)) {
			return false;
		}
		RelationModel relModel = getModel(rel);
		rel.setCreateTime(new Date());
		insert(rel, relModel);
		return true;
	}	
	
	public RelationModel getModel(Relation rel) {
		return TableModels.getModel(rel.getClass());
	}
	
	@SuppressWarnings("unchecked")
	public <R extends Relation> List<R> query(R rel) {
		TabQuery query = buildQuery(rel);
		RelationModel model = getModel(rel);
		List<R> list = dao.query(query, new TableRowMapper(model));
		return list;
	}
	
	public <R extends Relation> R newRelation(Class<R> relClass, 
			Class<? extends Entity> class1, Object id1, Object id2) {
		try {
			R rel = relClass.newInstance();
			RelationModel relModel = getModel(rel);
			FieldInfo[] keyFields = relModel.getKeyFields(class1);
			keyFields[0].setValue(rel, id1);
			keyFields[1].setValue(rel, id2);
			return rel;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Record toRecord(Relation rel, String[] fields) {
		if (rel==null || fields==null) {
			return null;
		}
		RelationModel model = getModel(rel);
		Record rec = new Record();
		FieldInfo field;
		Object value;
		for (String fieldName: fields) {
			field = model.getField(fieldName);
			if (field!=null) {
				value = field.getValue(rel);
				rec.put(fieldName, value);
			}
		}
		return rec;
	}
	
	public Record toRecord(Relation rel) {
		RelationModel model = getModel(rel);
		if (model!=null) {
			String[] fieldNames = model.getFieldsExcept();
			return toRecord(rel, fieldNames);
		}
		return null;
	}
	
	public Record toRecordNoKey(Relation rel) {
		RelationModel model = getModel(rel);
		if (model!=null) {
			String[] fieldNames = model.getFieldsExcept();
			return toRecord(rel, fieldNames);
		}
		return null;
	}

	public <R extends Relation> void update(R rel) {
		
		if (rel==null) {
			return;
		}
		if (!FromOrmHelper.isFromOrm(rel)) {
			throw new IntegError("object is not created by integ, can not be update.");
		}
		R old = this.querySingle(rel);
		if (old==null) {
			return;
		}

		RelationModel relModel = TableModels.getModel(rel.getClass());
		
		Map<String, Object> updateFields = calcUpdataFields(old, rel, relModel);

		if (updateFields.size()==0) {
			return ;
		}
		
		TabQuery query = buildQuery(rel);
		
		StatementAndValue where =  query.getWhere().toStatementAndValue();
		
		dao.update(relModel.getTableName(), updateFields, where);
		
	}
	
}
