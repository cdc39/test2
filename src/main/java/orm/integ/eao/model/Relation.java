package orm.integ.eao.model;

import java.util.Date;

public class Relation extends RecordObject {

	private Date createTime;

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
