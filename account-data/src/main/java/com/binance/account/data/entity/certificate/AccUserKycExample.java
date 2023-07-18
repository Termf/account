package com.binance.account.data.entity.certificate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class AccUserKycExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public AccUserKycExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        protected void addCriterionForJDBCDate(String condition, Date value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value.getTime()), property);
        }

        protected void addCriterionForJDBCDate(String condition, List<Date> values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            List<java.sql.Date> dateList = new ArrayList<java.sql.Date>();
            Iterator<Date> iter = values.iterator();
            while (iter.hasNext()) {
                dateList.add(new java.sql.Date(iter.next().getTime()));
            }
            addCriterion(condition, dateList, property);
        }

        protected void addCriterionForJDBCDate(String condition, Date value1, Date value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value1.getTime()), new java.sql.Date(value2.getTime()), property);
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNull() {
            addCriterion("user_id is null");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualTo(Long value) {
            addCriterion("user_id =", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualTo(Long value) {
            addCriterion("user_id <>", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThan(Long value) {
            addCriterion("user_id >", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(Long value) {
            addCriterion("user_id >=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThan(Long value) {
            addCriterion("user_id <", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualTo(Long value) {
            addCriterion("user_id <=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdIn(List<Long> values) {
            addCriterion("user_id in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotIn(List<Long> values) {
            addCriterion("user_id not in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdBetween(Long value1, Long value2) {
            addCriterion("user_id between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotBetween(Long value1, Long value2) {
            addCriterion("user_id not between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Byte value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Byte value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Byte value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Byte value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Byte> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Byte> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andJumioIdIsNull() {
            addCriterion("jumio_id is null");
            return (Criteria) this;
        }

        public Criteria andJumioIdIsNotNull() {
            addCriterion("jumio_id is not null");
            return (Criteria) this;
        }

        public Criteria andJumioIdEqualTo(String value) {
            addCriterion("jumio_id =", value, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdNotEqualTo(String value) {
            addCriterion("jumio_id <>", value, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdGreaterThan(String value) {
            addCriterion("jumio_id >", value, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdGreaterThanOrEqualTo(String value) {
            addCriterion("jumio_id >=", value, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdLessThan(String value) {
            addCriterion("jumio_id <", value, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdLessThanOrEqualTo(String value) {
            addCriterion("jumio_id <=", value, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdLike(String value) {
            addCriterion("jumio_id like", value, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdNotLike(String value) {
            addCriterion("jumio_id not like", value, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdIn(List<String> values) {
            addCriterion("jumio_id in", values, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdNotIn(List<String> values) {
            addCriterion("jumio_id not in", values, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdBetween(String value1, String value2) {
            addCriterion("jumio_id between", value1, value2, "jumioId");
            return (Criteria) this;
        }

        public Criteria andJumioIdNotBetween(String value1, String value2) {
            addCriterion("jumio_id not between", value1, value2, "jumioId");
            return (Criteria) this;
        }

        public Criteria andScanReferenceIsNull() {
            addCriterion("scan_reference is null");
            return (Criteria) this;
        }

        public Criteria andScanReferenceIsNotNull() {
            addCriterion("scan_reference is not null");
            return (Criteria) this;
        }

        public Criteria andScanReferenceEqualTo(String value) {
            addCriterion("scan_reference =", value, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceNotEqualTo(String value) {
            addCriterion("scan_reference <>", value, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceGreaterThan(String value) {
            addCriterion("scan_reference >", value, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceGreaterThanOrEqualTo(String value) {
            addCriterion("scan_reference >=", value, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceLessThan(String value) {
            addCriterion("scan_reference <", value, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceLessThanOrEqualTo(String value) {
            addCriterion("scan_reference <=", value, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceLike(String value) {
            addCriterion("scan_reference like", value, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceNotLike(String value) {
            addCriterion("scan_reference not like", value, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceIn(List<String> values) {
            addCriterion("scan_reference in", values, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceNotIn(List<String> values) {
            addCriterion("scan_reference not in", values, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceBetween(String value1, String value2) {
            addCriterion("scan_reference between", value1, value2, "scanReference");
            return (Criteria) this;
        }

        public Criteria andScanReferenceNotBetween(String value1, String value2) {
            addCriterion("scan_reference not between", value1, value2, "scanReference");
            return (Criteria) this;
        }

        public Criteria andFrontIsNull() {
            addCriterion("front is null");
            return (Criteria) this;
        }

        public Criteria andFrontIsNotNull() {
            addCriterion("front is not null");
            return (Criteria) this;
        }

        public Criteria andFrontEqualTo(String value) {
            addCriterion("front =", value, "front");
            return (Criteria) this;
        }

        public Criteria andFrontNotEqualTo(String value) {
            addCriterion("front <>", value, "front");
            return (Criteria) this;
        }

        public Criteria andFrontGreaterThan(String value) {
            addCriterion("front >", value, "front");
            return (Criteria) this;
        }

        public Criteria andFrontGreaterThanOrEqualTo(String value) {
            addCriterion("front >=", value, "front");
            return (Criteria) this;
        }

        public Criteria andFrontLessThan(String value) {
            addCriterion("front <", value, "front");
            return (Criteria) this;
        }

        public Criteria andFrontLessThanOrEqualTo(String value) {
            addCriterion("front <=", value, "front");
            return (Criteria) this;
        }

        public Criteria andFrontLike(String value) {
            addCriterion("front like", value, "front");
            return (Criteria) this;
        }

        public Criteria andFrontNotLike(String value) {
            addCriterion("front not like", value, "front");
            return (Criteria) this;
        }

        public Criteria andFrontIn(List<String> values) {
            addCriterion("front in", values, "front");
            return (Criteria) this;
        }

        public Criteria andFrontNotIn(List<String> values) {
            addCriterion("front not in", values, "front");
            return (Criteria) this;
        }

        public Criteria andFrontBetween(String value1, String value2) {
            addCriterion("front between", value1, value2, "front");
            return (Criteria) this;
        }

        public Criteria andFrontNotBetween(String value1, String value2) {
            addCriterion("front not between", value1, value2, "front");
            return (Criteria) this;
        }

        public Criteria andBackIsNull() {
            addCriterion("back is null");
            return (Criteria) this;
        }

        public Criteria andBackIsNotNull() {
            addCriterion("back is not null");
            return (Criteria) this;
        }

        public Criteria andBackEqualTo(String value) {
            addCriterion("back =", value, "back");
            return (Criteria) this;
        }

        public Criteria andBackNotEqualTo(String value) {
            addCriterion("back <>", value, "back");
            return (Criteria) this;
        }

        public Criteria andBackGreaterThan(String value) {
            addCriterion("back >", value, "back");
            return (Criteria) this;
        }

        public Criteria andBackGreaterThanOrEqualTo(String value) {
            addCriterion("back >=", value, "back");
            return (Criteria) this;
        }

        public Criteria andBackLessThan(String value) {
            addCriterion("back <", value, "back");
            return (Criteria) this;
        }

        public Criteria andBackLessThanOrEqualTo(String value) {
            addCriterion("back <=", value, "back");
            return (Criteria) this;
        }

        public Criteria andBackLike(String value) {
            addCriterion("back like", value, "back");
            return (Criteria) this;
        }

        public Criteria andBackNotLike(String value) {
            addCriterion("back not like", value, "back");
            return (Criteria) this;
        }

        public Criteria andBackIn(List<String> values) {
            addCriterion("back in", values, "back");
            return (Criteria) this;
        }

        public Criteria andBackNotIn(List<String> values) {
            addCriterion("back not in", values, "back");
            return (Criteria) this;
        }

        public Criteria andBackBetween(String value1, String value2) {
            addCriterion("back between", value1, value2, "back");
            return (Criteria) this;
        }

        public Criteria andBackNotBetween(String value1, String value2) {
            addCriterion("back not between", value1, value2, "back");
            return (Criteria) this;
        }

        public Criteria andFaceIsNull() {
            addCriterion("face is null");
            return (Criteria) this;
        }

        public Criteria andFaceIsNotNull() {
            addCriterion("face is not null");
            return (Criteria) this;
        }

        public Criteria andFaceEqualTo(String value) {
            addCriterion("face =", value, "face");
            return (Criteria) this;
        }

        public Criteria andFaceNotEqualTo(String value) {
            addCriterion("face <>", value, "face");
            return (Criteria) this;
        }

        public Criteria andFaceGreaterThan(String value) {
            addCriterion("face >", value, "face");
            return (Criteria) this;
        }

        public Criteria andFaceGreaterThanOrEqualTo(String value) {
            addCriterion("face >=", value, "face");
            return (Criteria) this;
        }

        public Criteria andFaceLessThan(String value) {
            addCriterion("face <", value, "face");
            return (Criteria) this;
        }

        public Criteria andFaceLessThanOrEqualTo(String value) {
            addCriterion("face <=", value, "face");
            return (Criteria) this;
        }

        public Criteria andFaceLike(String value) {
            addCriterion("face like", value, "face");
            return (Criteria) this;
        }

        public Criteria andFaceNotLike(String value) {
            addCriterion("face not like", value, "face");
            return (Criteria) this;
        }

        public Criteria andFaceIn(List<String> values) {
            addCriterion("face in", values, "face");
            return (Criteria) this;
        }

        public Criteria andFaceNotIn(List<String> values) {
            addCriterion("face not in", values, "face");
            return (Criteria) this;
        }

        public Criteria andFaceBetween(String value1, String value2) {
            addCriterion("face between", value1, value2, "face");
            return (Criteria) this;
        }

        public Criteria andFaceNotBetween(String value1, String value2) {
            addCriterion("face not between", value1, value2, "face");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameIsNull() {
            addCriterion("fill_first_name is null");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameIsNotNull() {
            addCriterion("fill_first_name is not null");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameEqualTo(String value) {
            addCriterion("fill_first_name =", value, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameNotEqualTo(String value) {
            addCriterion("fill_first_name <>", value, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameGreaterThan(String value) {
            addCriterion("fill_first_name >", value, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameGreaterThanOrEqualTo(String value) {
            addCriterion("fill_first_name >=", value, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameLessThan(String value) {
            addCriterion("fill_first_name <", value, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameLessThanOrEqualTo(String value) {
            addCriterion("fill_first_name <=", value, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameLike(String value) {
            addCriterion("fill_first_name like", value, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameNotLike(String value) {
            addCriterion("fill_first_name not like", value, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameIn(List<String> values) {
            addCriterion("fill_first_name in", values, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameNotIn(List<String> values) {
            addCriterion("fill_first_name not in", values, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameBetween(String value1, String value2) {
            addCriterion("fill_first_name between", value1, value2, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillFirstNameNotBetween(String value1, String value2) {
            addCriterion("fill_first_name not between", value1, value2, "fillFirstName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameIsNull() {
            addCriterion("fill_middle_name is null");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameIsNotNull() {
            addCriterion("fill_middle_name is not null");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameEqualTo(String value) {
            addCriterion("fill_middle_name =", value, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameNotEqualTo(String value) {
            addCriterion("fill_middle_name <>", value, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameGreaterThan(String value) {
            addCriterion("fill_middle_name >", value, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameGreaterThanOrEqualTo(String value) {
            addCriterion("fill_middle_name >=", value, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameLessThan(String value) {
            addCriterion("fill_middle_name <", value, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameLessThanOrEqualTo(String value) {
            addCriterion("fill_middle_name <=", value, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameLike(String value) {
            addCriterion("fill_middle_name like", value, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameNotLike(String value) {
            addCriterion("fill_middle_name not like", value, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameIn(List<String> values) {
            addCriterion("fill_middle_name in", values, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameNotIn(List<String> values) {
            addCriterion("fill_middle_name not in", values, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameBetween(String value1, String value2) {
            addCriterion("fill_middle_name between", value1, value2, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillMiddleNameNotBetween(String value1, String value2) {
            addCriterion("fill_middle_name not between", value1, value2, "fillMiddleName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameIsNull() {
            addCriterion("fill_last_name is null");
            return (Criteria) this;
        }

        public Criteria andFillLastNameIsNotNull() {
            addCriterion("fill_last_name is not null");
            return (Criteria) this;
        }

        public Criteria andFillLastNameEqualTo(String value) {
            addCriterion("fill_last_name =", value, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameNotEqualTo(String value) {
            addCriterion("fill_last_name <>", value, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameGreaterThan(String value) {
            addCriterion("fill_last_name >", value, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameGreaterThanOrEqualTo(String value) {
            addCriterion("fill_last_name >=", value, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameLessThan(String value) {
            addCriterion("fill_last_name <", value, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameLessThanOrEqualTo(String value) {
            addCriterion("fill_last_name <=", value, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameLike(String value) {
            addCriterion("fill_last_name like", value, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameNotLike(String value) {
            addCriterion("fill_last_name not like", value, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameIn(List<String> values) {
            addCriterion("fill_last_name in", values, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameNotIn(List<String> values) {
            addCriterion("fill_last_name not in", values, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameBetween(String value1, String value2) {
            addCriterion("fill_last_name between", value1, value2, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillLastNameNotBetween(String value1, String value2) {
            addCriterion("fill_last_name not between", value1, value2, "fillLastName");
            return (Criteria) this;
        }

        public Criteria andFillDobIsNull() {
            addCriterion("fill_dob is null");
            return (Criteria) this;
        }

        public Criteria andFillDobIsNotNull() {
            addCriterion("fill_dob is not null");
            return (Criteria) this;
        }

        public Criteria andFillDobEqualTo(Date value) {
            addCriterionForJDBCDate("fill_dob =", value, "fillDob");
            return (Criteria) this;
        }

        public Criteria andFillDobNotEqualTo(Date value) {
            addCriterionForJDBCDate("fill_dob <>", value, "fillDob");
            return (Criteria) this;
        }

        public Criteria andFillDobGreaterThan(Date value) {
            addCriterionForJDBCDate("fill_dob >", value, "fillDob");
            return (Criteria) this;
        }

        public Criteria andFillDobGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("fill_dob >=", value, "fillDob");
            return (Criteria) this;
        }

        public Criteria andFillDobLessThan(Date value) {
            addCriterionForJDBCDate("fill_dob <", value, "fillDob");
            return (Criteria) this;
        }

        public Criteria andFillDobLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("fill_dob <=", value, "fillDob");
            return (Criteria) this;
        }

        public Criteria andFillDobIn(List<Date> values) {
            addCriterionForJDBCDate("fill_dob in", values, "fillDob");
            return (Criteria) this;
        }

        public Criteria andFillDobNotIn(List<Date> values) {
            addCriterionForJDBCDate("fill_dob not in", values, "fillDob");
            return (Criteria) this;
        }

        public Criteria andFillDobBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("fill_dob between", value1, value2, "fillDob");
            return (Criteria) this;
        }

        public Criteria andFillDobNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("fill_dob not between", value1, value2, "fillDob");
            return (Criteria) this;
        }

        public Criteria andFillAddressIsNull() {
            addCriterion("fill_address is null");
            return (Criteria) this;
        }

        public Criteria andFillAddressIsNotNull() {
            addCriterion("fill_address is not null");
            return (Criteria) this;
        }

        public Criteria andFillAddressEqualTo(String value) {
            addCriterion("fill_address =", value, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressNotEqualTo(String value) {
            addCriterion("fill_address <>", value, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressGreaterThan(String value) {
            addCriterion("fill_address >", value, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressGreaterThanOrEqualTo(String value) {
            addCriterion("fill_address >=", value, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressLessThan(String value) {
            addCriterion("fill_address <", value, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressLessThanOrEqualTo(String value) {
            addCriterion("fill_address <=", value, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressLike(String value) {
            addCriterion("fill_address like", value, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressNotLike(String value) {
            addCriterion("fill_address not like", value, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressIn(List<String> values) {
            addCriterion("fill_address in", values, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressNotIn(List<String> values) {
            addCriterion("fill_address not in", values, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressBetween(String value1, String value2) {
            addCriterion("fill_address between", value1, value2, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillAddressNotBetween(String value1, String value2) {
            addCriterion("fill_address not between", value1, value2, "fillAddress");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeIsNull() {
            addCriterion("fill_postal_code is null");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeIsNotNull() {
            addCriterion("fill_postal_code is not null");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeEqualTo(String value) {
            addCriterion("fill_postal_code =", value, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeNotEqualTo(String value) {
            addCriterion("fill_postal_code <>", value, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeGreaterThan(String value) {
            addCriterion("fill_postal_code >", value, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeGreaterThanOrEqualTo(String value) {
            addCriterion("fill_postal_code >=", value, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeLessThan(String value) {
            addCriterion("fill_postal_code <", value, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeLessThanOrEqualTo(String value) {
            addCriterion("fill_postal_code <=", value, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeLike(String value) {
            addCriterion("fill_postal_code like", value, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeNotLike(String value) {
            addCriterion("fill_postal_code not like", value, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeIn(List<String> values) {
            addCriterion("fill_postal_code in", values, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeNotIn(List<String> values) {
            addCriterion("fill_postal_code not in", values, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeBetween(String value1, String value2) {
            addCriterion("fill_postal_code between", value1, value2, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillPostalCodeNotBetween(String value1, String value2) {
            addCriterion("fill_postal_code not between", value1, value2, "fillPostalCode");
            return (Criteria) this;
        }

        public Criteria andFillCityIsNull() {
            addCriterion("fill_city is null");
            return (Criteria) this;
        }

        public Criteria andFillCityIsNotNull() {
            addCriterion("fill_city is not null");
            return (Criteria) this;
        }

        public Criteria andFillCityEqualTo(String value) {
            addCriterion("fill_city =", value, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityNotEqualTo(String value) {
            addCriterion("fill_city <>", value, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityGreaterThan(String value) {
            addCriterion("fill_city >", value, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityGreaterThanOrEqualTo(String value) {
            addCriterion("fill_city >=", value, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityLessThan(String value) {
            addCriterion("fill_city <", value, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityLessThanOrEqualTo(String value) {
            addCriterion("fill_city <=", value, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityLike(String value) {
            addCriterion("fill_city like", value, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityNotLike(String value) {
            addCriterion("fill_city not like", value, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityIn(List<String> values) {
            addCriterion("fill_city in", values, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityNotIn(List<String> values) {
            addCriterion("fill_city not in", values, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityBetween(String value1, String value2) {
            addCriterion("fill_city between", value1, value2, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCityNotBetween(String value1, String value2) {
            addCriterion("fill_city not between", value1, value2, "fillCity");
            return (Criteria) this;
        }

        public Criteria andFillCountryIsNull() {
            addCriterion("fill_country is null");
            return (Criteria) this;
        }

        public Criteria andFillCountryIsNotNull() {
            addCriterion("fill_country is not null");
            return (Criteria) this;
        }

        public Criteria andFillCountryEqualTo(String value) {
            addCriterion("fill_country =", value, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryNotEqualTo(String value) {
            addCriterion("fill_country <>", value, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryGreaterThan(String value) {
            addCriterion("fill_country >", value, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryGreaterThanOrEqualTo(String value) {
            addCriterion("fill_country >=", value, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryLessThan(String value) {
            addCriterion("fill_country <", value, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryLessThanOrEqualTo(String value) {
            addCriterion("fill_country <=", value, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryLike(String value) {
            addCriterion("fill_country like", value, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryNotLike(String value) {
            addCriterion("fill_country not like", value, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryIn(List<String> values) {
            addCriterion("fill_country in", values, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryNotIn(List<String> values) {
            addCriterion("fill_country not in", values, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryBetween(String value1, String value2) {
            addCriterion("fill_country between", value1, value2, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFillCountryNotBetween(String value1, String value2) {
            addCriterion("fill_country not between", value1, value2, "fillCountry");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameIsNull() {
            addCriterion("former_first_name is null");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameIsNotNull() {
            addCriterion("former_first_name is not null");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameEqualTo(String value) {
            addCriterion("former_first_name =", value, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameNotEqualTo(String value) {
            addCriterion("former_first_name <>", value, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameGreaterThan(String value) {
            addCriterion("former_first_name >", value, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameGreaterThanOrEqualTo(String value) {
            addCriterion("former_first_name >=", value, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameLessThan(String value) {
            addCriterion("former_first_name <", value, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameLessThanOrEqualTo(String value) {
            addCriterion("former_first_name <=", value, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameLike(String value) {
            addCriterion("former_first_name like", value, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameNotLike(String value) {
            addCriterion("former_first_name not like", value, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameIn(List<String> values) {
            addCriterion("former_first_name in", values, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameNotIn(List<String> values) {
            addCriterion("former_first_name not in", values, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameBetween(String value1, String value2) {
            addCriterion("former_first_name between", value1, value2, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerFirstNameNotBetween(String value1, String value2) {
            addCriterion("former_first_name not between", value1, value2, "formerFirstName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameIsNull() {
            addCriterion("former_middle_name is null");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameIsNotNull() {
            addCriterion("former_middle_name is not null");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameEqualTo(String value) {
            addCriterion("former_middle_name =", value, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameNotEqualTo(String value) {
            addCriterion("former_middle_name <>", value, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameGreaterThan(String value) {
            addCriterion("former_middle_name >", value, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameGreaterThanOrEqualTo(String value) {
            addCriterion("former_middle_name >=", value, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameLessThan(String value) {
            addCriterion("former_middle_name <", value, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameLessThanOrEqualTo(String value) {
            addCriterion("former_middle_name <=", value, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameLike(String value) {
            addCriterion("former_middle_name like", value, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameNotLike(String value) {
            addCriterion("former_middle_name not like", value, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameIn(List<String> values) {
            addCriterion("former_middle_name in", values, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameNotIn(List<String> values) {
            addCriterion("former_middle_name not in", values, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameBetween(String value1, String value2) {
            addCriterion("former_middle_name between", value1, value2, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerMiddleNameNotBetween(String value1, String value2) {
            addCriterion("former_middle_name not between", value1, value2, "formerMiddleName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameIsNull() {
            addCriterion("former_last_name is null");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameIsNotNull() {
            addCriterion("former_last_name is not null");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameEqualTo(String value) {
            addCriterion("former_last_name =", value, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameNotEqualTo(String value) {
            addCriterion("former_last_name <>", value, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameGreaterThan(String value) {
            addCriterion("former_last_name >", value, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameGreaterThanOrEqualTo(String value) {
            addCriterion("former_last_name >=", value, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameLessThan(String value) {
            addCriterion("former_last_name <", value, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameLessThanOrEqualTo(String value) {
            addCriterion("former_last_name <=", value, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameLike(String value) {
            addCriterion("former_last_name like", value, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameNotLike(String value) {
            addCriterion("former_last_name not like", value, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameIn(List<String> values) {
            addCriterion("former_last_name in", values, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameNotIn(List<String> values) {
            addCriterion("former_last_name not in", values, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameBetween(String value1, String value2) {
            addCriterion("former_last_name between", value1, value2, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andFormerLastNameNotBetween(String value1, String value2) {
            addCriterion("former_last_name not between", value1, value2, "formerLastName");
            return (Criteria) this;
        }

        public Criteria andNationalityIsNull() {
            addCriterion("nationality is null");
            return (Criteria) this;
        }

        public Criteria andNationalityIsNotNull() {
            addCriterion("nationality is not null");
            return (Criteria) this;
        }

        public Criteria andNationalityEqualTo(String value) {
            addCriterion("nationality =", value, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityNotEqualTo(String value) {
            addCriterion("nationality <>", value, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityGreaterThan(String value) {
            addCriterion("nationality >", value, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityGreaterThanOrEqualTo(String value) {
            addCriterion("nationality >=", value, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityLessThan(String value) {
            addCriterion("nationality <", value, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityLessThanOrEqualTo(String value) {
            addCriterion("nationality <=", value, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityLike(String value) {
            addCriterion("nationality like", value, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityNotLike(String value) {
            addCriterion("nationality not like", value, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityIn(List<String> values) {
            addCriterion("nationality in", values, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityNotIn(List<String> values) {
            addCriterion("nationality not in", values, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityBetween(String value1, String value2) {
            addCriterion("nationality between", value1, value2, "nationality");
            return (Criteria) this;
        }

        public Criteria andNationalityNotBetween(String value1, String value2) {
            addCriterion("nationality not between", value1, value2, "nationality");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameIsNull() {
            addCriterion("check_first_name is null");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameIsNotNull() {
            addCriterion("check_first_name is not null");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameEqualTo(String value) {
            addCriterion("check_first_name =", value, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameNotEqualTo(String value) {
            addCriterion("check_first_name <>", value, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameGreaterThan(String value) {
            addCriterion("check_first_name >", value, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameGreaterThanOrEqualTo(String value) {
            addCriterion("check_first_name >=", value, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameLessThan(String value) {
            addCriterion("check_first_name <", value, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameLessThanOrEqualTo(String value) {
            addCriterion("check_first_name <=", value, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameLike(String value) {
            addCriterion("check_first_name like", value, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameNotLike(String value) {
            addCriterion("check_first_name not like", value, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameIn(List<String> values) {
            addCriterion("check_first_name in", values, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameNotIn(List<String> values) {
            addCriterion("check_first_name not in", values, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameBetween(String value1, String value2) {
            addCriterion("check_first_name between", value1, value2, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckFirstNameNotBetween(String value1, String value2) {
            addCriterion("check_first_name not between", value1, value2, "checkFirstName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameIsNull() {
            addCriterion("check_last_name is null");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameIsNotNull() {
            addCriterion("check_last_name is not null");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameEqualTo(String value) {
            addCriterion("check_last_name =", value, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameNotEqualTo(String value) {
            addCriterion("check_last_name <>", value, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameGreaterThan(String value) {
            addCriterion("check_last_name >", value, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameGreaterThanOrEqualTo(String value) {
            addCriterion("check_last_name >=", value, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameLessThan(String value) {
            addCriterion("check_last_name <", value, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameLessThanOrEqualTo(String value) {
            addCriterion("check_last_name <=", value, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameLike(String value) {
            addCriterion("check_last_name like", value, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameNotLike(String value) {
            addCriterion("check_last_name not like", value, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameIn(List<String> values) {
            addCriterion("check_last_name in", values, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameNotIn(List<String> values) {
            addCriterion("check_last_name not in", values, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameBetween(String value1, String value2) {
            addCriterion("check_last_name between", value1, value2, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckLastNameNotBetween(String value1, String value2) {
            addCriterion("check_last_name not between", value1, value2, "checkLastName");
            return (Criteria) this;
        }

        public Criteria andCheckDobIsNull() {
            addCriterion("check_dob is null");
            return (Criteria) this;
        }

        public Criteria andCheckDobIsNotNull() {
            addCriterion("check_dob is not null");
            return (Criteria) this;
        }

        public Criteria andCheckDobEqualTo(String value) {
            addCriterion("check_dob =", value, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobNotEqualTo(String value) {
            addCriterion("check_dob <>", value, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobGreaterThan(String value) {
            addCriterion("check_dob >", value, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobGreaterThanOrEqualTo(String value) {
            addCriterion("check_dob >=", value, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobLessThan(String value) {
            addCriterion("check_dob <", value, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobLessThanOrEqualTo(String value) {
            addCriterion("check_dob <=", value, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobLike(String value) {
            addCriterion("check_dob like", value, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobNotLike(String value) {
            addCriterion("check_dob not like", value, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobIn(List<String> values) {
            addCriterion("check_dob in", values, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobNotIn(List<String> values) {
            addCriterion("check_dob not in", values, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobBetween(String value1, String value2) {
            addCriterion("check_dob between", value1, value2, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckDobNotBetween(String value1, String value2) {
            addCriterion("check_dob not between", value1, value2, "checkDob");
            return (Criteria) this;
        }

        public Criteria andCheckAddressIsNull() {
            addCriterion("check_address is null");
            return (Criteria) this;
        }

        public Criteria andCheckAddressIsNotNull() {
            addCriterion("check_address is not null");
            return (Criteria) this;
        }

        public Criteria andCheckAddressEqualTo(String value) {
            addCriterion("check_address =", value, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressNotEqualTo(String value) {
            addCriterion("check_address <>", value, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressGreaterThan(String value) {
            addCriterion("check_address >", value, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressGreaterThanOrEqualTo(String value) {
            addCriterion("check_address >=", value, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressLessThan(String value) {
            addCriterion("check_address <", value, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressLessThanOrEqualTo(String value) {
            addCriterion("check_address <=", value, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressLike(String value) {
            addCriterion("check_address like", value, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressNotLike(String value) {
            addCriterion("check_address not like", value, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressIn(List<String> values) {
            addCriterion("check_address in", values, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressNotIn(List<String> values) {
            addCriterion("check_address not in", values, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressBetween(String value1, String value2) {
            addCriterion("check_address between", value1, value2, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckAddressNotBetween(String value1, String value2) {
            addCriterion("check_address not between", value1, value2, "checkAddress");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeIsNull() {
            addCriterion("check_postal_code is null");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeIsNotNull() {
            addCriterion("check_postal_code is not null");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeEqualTo(String value) {
            addCriterion("check_postal_code =", value, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeNotEqualTo(String value) {
            addCriterion("check_postal_code <>", value, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeGreaterThan(String value) {
            addCriterion("check_postal_code >", value, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeGreaterThanOrEqualTo(String value) {
            addCriterion("check_postal_code >=", value, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeLessThan(String value) {
            addCriterion("check_postal_code <", value, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeLessThanOrEqualTo(String value) {
            addCriterion("check_postal_code <=", value, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeLike(String value) {
            addCriterion("check_postal_code like", value, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeNotLike(String value) {
            addCriterion("check_postal_code not like", value, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeIn(List<String> values) {
            addCriterion("check_postal_code in", values, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeNotIn(List<String> values) {
            addCriterion("check_postal_code not in", values, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeBetween(String value1, String value2) {
            addCriterion("check_postal_code between", value1, value2, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckPostalCodeNotBetween(String value1, String value2) {
            addCriterion("check_postal_code not between", value1, value2, "checkPostalCode");
            return (Criteria) this;
        }

        public Criteria andCheckCityIsNull() {
            addCriterion("check_city is null");
            return (Criteria) this;
        }

        public Criteria andCheckCityIsNotNull() {
            addCriterion("check_city is not null");
            return (Criteria) this;
        }

        public Criteria andCheckCityEqualTo(String value) {
            addCriterion("check_city =", value, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityNotEqualTo(String value) {
            addCriterion("check_city <>", value, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityGreaterThan(String value) {
            addCriterion("check_city >", value, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityGreaterThanOrEqualTo(String value) {
            addCriterion("check_city >=", value, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityLessThan(String value) {
            addCriterion("check_city <", value, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityLessThanOrEqualTo(String value) {
            addCriterion("check_city <=", value, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityLike(String value) {
            addCriterion("check_city like", value, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityNotLike(String value) {
            addCriterion("check_city not like", value, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityIn(List<String> values) {
            addCriterion("check_city in", values, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityNotIn(List<String> values) {
            addCriterion("check_city not in", values, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityBetween(String value1, String value2) {
            addCriterion("check_city between", value1, value2, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckCityNotBetween(String value1, String value2) {
            addCriterion("check_city not between", value1, value2, "checkCity");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryIsNull() {
            addCriterion("check_issuing_country is null");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryIsNotNull() {
            addCriterion("check_issuing_country is not null");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryEqualTo(String value) {
            addCriterion("check_issuing_country =", value, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryNotEqualTo(String value) {
            addCriterion("check_issuing_country <>", value, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryGreaterThan(String value) {
            addCriterion("check_issuing_country >", value, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryGreaterThanOrEqualTo(String value) {
            addCriterion("check_issuing_country >=", value, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryLessThan(String value) {
            addCriterion("check_issuing_country <", value, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryLessThanOrEqualTo(String value) {
            addCriterion("check_issuing_country <=", value, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryLike(String value) {
            addCriterion("check_issuing_country like", value, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryNotLike(String value) {
            addCriterion("check_issuing_country not like", value, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryIn(List<String> values) {
            addCriterion("check_issuing_country in", values, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryNotIn(List<String> values) {
            addCriterion("check_issuing_country not in", values, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryBetween(String value1, String value2) {
            addCriterion("check_issuing_country between", value1, value2, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckIssuingCountryNotBetween(String value1, String value2) {
            addCriterion("check_issuing_country not between", value1, value2, "checkIssuingCountry");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateIsNull() {
            addCriterion("check_expiry_date is null");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateIsNotNull() {
            addCriterion("check_expiry_date is not null");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateEqualTo(String value) {
            addCriterion("check_expiry_date =", value, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateNotEqualTo(String value) {
            addCriterion("check_expiry_date <>", value, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateGreaterThan(String value) {
            addCriterion("check_expiry_date >", value, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateGreaterThanOrEqualTo(String value) {
            addCriterion("check_expiry_date >=", value, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateLessThan(String value) {
            addCriterion("check_expiry_date <", value, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateLessThanOrEqualTo(String value) {
            addCriterion("check_expiry_date <=", value, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateLike(String value) {
            addCriterion("check_expiry_date like", value, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateNotLike(String value) {
            addCriterion("check_expiry_date not like", value, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateIn(List<String> values) {
            addCriterion("check_expiry_date in", values, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateNotIn(List<String> values) {
            addCriterion("check_expiry_date not in", values, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateBetween(String value1, String value2) {
            addCriterion("check_expiry_date between", value1, value2, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckExpiryDateNotBetween(String value1, String value2) {
            addCriterion("check_expiry_date not between", value1, value2, "checkExpiryDate");
            return (Criteria) this;
        }

        public Criteria andCheckNumberIsNull() {
            addCriterion("check_number is null");
            return (Criteria) this;
        }

        public Criteria andCheckNumberIsNotNull() {
            addCriterion("check_number is not null");
            return (Criteria) this;
        }

        public Criteria andCheckNumberEqualTo(String value) {
            addCriterion("check_number =", value, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberNotEqualTo(String value) {
            addCriterion("check_number <>", value, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberGreaterThan(String value) {
            addCriterion("check_number >", value, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberGreaterThanOrEqualTo(String value) {
            addCriterion("check_number >=", value, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberLessThan(String value) {
            addCriterion("check_number <", value, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberLessThanOrEqualTo(String value) {
            addCriterion("check_number <=", value, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberLike(String value) {
            addCriterion("check_number like", value, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberNotLike(String value) {
            addCriterion("check_number not like", value, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberIn(List<String> values) {
            addCriterion("check_number in", values, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberNotIn(List<String> values) {
            addCriterion("check_number not in", values, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberBetween(String value1, String value2) {
            addCriterion("check_number between", value1, value2, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckNumberNotBetween(String value1, String value2) {
            addCriterion("check_number not between", value1, value2, "checkNumber");
            return (Criteria) this;
        }

        public Criteria andCheckTypeIsNull() {
            addCriterion("check_type is null");
            return (Criteria) this;
        }

        public Criteria andCheckTypeIsNotNull() {
            addCriterion("check_type is not null");
            return (Criteria) this;
        }

        public Criteria andCheckTypeEqualTo(String value) {
            addCriterion("check_type =", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeNotEqualTo(String value) {
            addCriterion("check_type <>", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeGreaterThan(String value) {
            addCriterion("check_type >", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeGreaterThanOrEqualTo(String value) {
            addCriterion("check_type >=", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeLessThan(String value) {
            addCriterion("check_type <", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeLessThanOrEqualTo(String value) {
            addCriterion("check_type <=", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeLike(String value) {
            addCriterion("check_type like", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeNotLike(String value) {
            addCriterion("check_type not like", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeIn(List<String> values) {
            addCriterion("check_type in", values, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeNotIn(List<String> values) {
            addCriterion("check_type not in", values, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeBetween(String value1, String value2) {
            addCriterion("check_type between", value1, value2, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeNotBetween(String value1, String value2) {
            addCriterion("check_type not between", value1, value2, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckStatusIsNull() {
            addCriterion("check_status is null");
            return (Criteria) this;
        }

        public Criteria andCheckStatusIsNotNull() {
            addCriterion("check_status is not null");
            return (Criteria) this;
        }

        public Criteria andCheckStatusEqualTo(String value) {
            addCriterion("check_status =", value, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusNotEqualTo(String value) {
            addCriterion("check_status <>", value, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusGreaterThan(String value) {
            addCriterion("check_status >", value, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusGreaterThanOrEqualTo(String value) {
            addCriterion("check_status >=", value, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusLessThan(String value) {
            addCriterion("check_status <", value, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusLessThanOrEqualTo(String value) {
            addCriterion("check_status <=", value, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusLike(String value) {
            addCriterion("check_status like", value, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusNotLike(String value) {
            addCriterion("check_status not like", value, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusIn(List<String> values) {
            addCriterion("check_status in", values, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusNotIn(List<String> values) {
            addCriterion("check_status not in", values, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusBetween(String value1, String value2) {
            addCriterion("check_status between", value1, value2, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusNotBetween(String value1, String value2) {
            addCriterion("check_status not between", value1, value2, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckSourceIsNull() {
            addCriterion("check_source is null");
            return (Criteria) this;
        }

        public Criteria andCheckSourceIsNotNull() {
            addCriterion("check_source is not null");
            return (Criteria) this;
        }

        public Criteria andCheckSourceEqualTo(String value) {
            addCriterion("check_source =", value, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceNotEqualTo(String value) {
            addCriterion("check_source <>", value, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceGreaterThan(String value) {
            addCriterion("check_source >", value, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceGreaterThanOrEqualTo(String value) {
            addCriterion("check_source >=", value, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceLessThan(String value) {
            addCriterion("check_source <", value, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceLessThanOrEqualTo(String value) {
            addCriterion("check_source <=", value, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceLike(String value) {
            addCriterion("check_source like", value, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceNotLike(String value) {
            addCriterion("check_source not like", value, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceIn(List<String> values) {
            addCriterion("check_source in", values, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceNotIn(List<String> values) {
            addCriterion("check_source not in", values, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceBetween(String value1, String value2) {
            addCriterion("check_source between", value1, value2, "checkSource");
            return (Criteria) this;
        }

        public Criteria andCheckSourceNotBetween(String value1, String value2) {
            addCriterion("check_source not between", value1, value2, "checkSource");
            return (Criteria) this;
        }

        public Criteria andFailReasonIsNull() {
            addCriterion("fail_reason is null");
            return (Criteria) this;
        }

        public Criteria andFailReasonIsNotNull() {
            addCriterion("fail_reason is not null");
            return (Criteria) this;
        }

        public Criteria andFailReasonEqualTo(String value) {
            addCriterion("fail_reason =", value, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonNotEqualTo(String value) {
            addCriterion("fail_reason <>", value, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonGreaterThan(String value) {
            addCriterion("fail_reason >", value, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonGreaterThanOrEqualTo(String value) {
            addCriterion("fail_reason >=", value, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonLessThan(String value) {
            addCriterion("fail_reason <", value, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonLessThanOrEqualTo(String value) {
            addCriterion("fail_reason <=", value, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonLike(String value) {
            addCriterion("fail_reason like", value, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonNotLike(String value) {
            addCriterion("fail_reason not like", value, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonIn(List<String> values) {
            addCriterion("fail_reason in", values, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonNotIn(List<String> values) {
            addCriterion("fail_reason not in", values, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonBetween(String value1, String value2) {
            addCriterion("fail_reason between", value1, value2, "failReason");
            return (Criteria) this;
        }

        public Criteria andFailReasonNotBetween(String value1, String value2) {
            addCriterion("fail_reason not between", value1, value2, "failReason");
            return (Criteria) this;
        }

        public Criteria andMemoIsNull() {
            addCriterion("memo is null");
            return (Criteria) this;
        }

        public Criteria andMemoIsNotNull() {
            addCriterion("memo is not null");
            return (Criteria) this;
        }

        public Criteria andMemoEqualTo(String value) {
            addCriterion("memo =", value, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoNotEqualTo(String value) {
            addCriterion("memo <>", value, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoGreaterThan(String value) {
            addCriterion("memo >", value, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoGreaterThanOrEqualTo(String value) {
            addCriterion("memo >=", value, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoLessThan(String value) {
            addCriterion("memo <", value, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoLessThanOrEqualTo(String value) {
            addCriterion("memo <=", value, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoLike(String value) {
            addCriterion("memo like", value, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoNotLike(String value) {
            addCriterion("memo not like", value, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoIn(List<String> values) {
            addCriterion("memo in", values, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoNotIn(List<String> values) {
            addCriterion("memo not in", values, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoBetween(String value1, String value2) {
            addCriterion("memo between", value1, value2, "memo");
            return (Criteria) this;
        }

        public Criteria andMemoNotBetween(String value1, String value2) {
            addCriterion("memo not between", value1, value2, "memo");
            return (Criteria) this;
        }

        public Criteria andFaceStatusIsNull() {
            addCriterion("face_status is null");
            return (Criteria) this;
        }

        public Criteria andFaceStatusIsNotNull() {
            addCriterion("face_status is not null");
            return (Criteria) this;
        }

        public Criteria andFaceStatusEqualTo(String value) {
            addCriterion("face_status =", value, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusNotEqualTo(String value) {
            addCriterion("face_status <>", value, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusGreaterThan(String value) {
            addCriterion("face_status >", value, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusGreaterThanOrEqualTo(String value) {
            addCriterion("face_status >=", value, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusLessThan(String value) {
            addCriterion("face_status <", value, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusLessThanOrEqualTo(String value) {
            addCriterion("face_status <=", value, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusLike(String value) {
            addCriterion("face_status like", value, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusNotLike(String value) {
            addCriterion("face_status not like", value, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusIn(List<String> values) {
            addCriterion("face_status in", values, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusNotIn(List<String> values) {
            addCriterion("face_status not in", values, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusBetween(String value1, String value2) {
            addCriterion("face_status between", value1, value2, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceStatusNotBetween(String value1, String value2) {
            addCriterion("face_status not between", value1, value2, "faceStatus");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkIsNull() {
            addCriterion("face_remark is null");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkIsNotNull() {
            addCriterion("face_remark is not null");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkEqualTo(String value) {
            addCriterion("face_remark =", value, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkNotEqualTo(String value) {
            addCriterion("face_remark <>", value, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkGreaterThan(String value) {
            addCriterion("face_remark >", value, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("face_remark >=", value, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkLessThan(String value) {
            addCriterion("face_remark <", value, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkLessThanOrEqualTo(String value) {
            addCriterion("face_remark <=", value, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkLike(String value) {
            addCriterion("face_remark like", value, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkNotLike(String value) {
            addCriterion("face_remark not like", value, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkIn(List<String> values) {
            addCriterion("face_remark in", values, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkNotIn(List<String> values) {
            addCriterion("face_remark not in", values, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkBetween(String value1, String value2) {
            addCriterion("face_remark between", value1, value2, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andFaceRemarkNotBetween(String value1, String value2) {
            addCriterion("face_remark not between", value1, value2, "faceRemark");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdIsNull() {
            addCriterion("trans_face_log_id is null");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdIsNotNull() {
            addCriterion("trans_face_log_id is not null");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdEqualTo(String value) {
            addCriterion("trans_face_log_id =", value, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdNotEqualTo(String value) {
            addCriterion("trans_face_log_id <>", value, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdGreaterThan(String value) {
            addCriterion("trans_face_log_id >", value, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdGreaterThanOrEqualTo(String value) {
            addCriterion("trans_face_log_id >=", value, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdLessThan(String value) {
            addCriterion("trans_face_log_id <", value, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdLessThanOrEqualTo(String value) {
            addCriterion("trans_face_log_id <=", value, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdLike(String value) {
            addCriterion("trans_face_log_id like", value, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdNotLike(String value) {
            addCriterion("trans_face_log_id not like", value, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdIn(List<String> values) {
            addCriterion("trans_face_log_id in", values, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdNotIn(List<String> values) {
            addCriterion("trans_face_log_id not in", values, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdBetween(String value1, String value2) {
            addCriterion("trans_face_log_id between", value1, value2, "transFaceLogId");
            return (Criteria) this;
        }

        public Criteria andTransFaceLogIdNotBetween(String value1, String value2) {
            addCriterion("trans_face_log_id not between", value1, value2, "transFaceLogId");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}