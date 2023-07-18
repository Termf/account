package com.binance.account.data.entity.security;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestionRepository implements Serializable {
	private static final long serialVersionUID = -3336197836983473904L;
	/* question_repository */
	private Long id;
	private String riskType;
	private String docLangFlag;
	private String remark;
	private Date createTime;
	private Date updateTime;
	private byte enable = 0;// 0-启用;1-禁用
	private String group; // 属于那套题
	private Integer weight; //问题权重
	
	/* risk的问题选项,不保存数据库 */
	public List<String> options;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((riskType == null) ? 0 : riskType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuestionRepository other = (QuestionRepository) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (riskType == null) {
			if (other.riskType != null)
				return false;
		} else if (!riskType.equals(other.riskType))
			return false;
		return true;
	}
}
