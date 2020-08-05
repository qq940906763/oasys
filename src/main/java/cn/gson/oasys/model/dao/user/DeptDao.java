package cn.gson.oasys.model.dao.user;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import cn.gson.oasys.model.entity.user.Dept;

public interface DeptDao extends PagingAndSortingRepository<Dept, Long>{

	List<Dept> findByDeptId(Long id);
	
	
	@Query("select de.deptName from Dept de where de.deptId=:id")
	String findname(@Param("id")Long id);

	/**
	 * 根据系统id查询系统下部门
	 * @param sysId
	 * @return
	 */
	@Query("from Dept de where de.sysId=:sysId")
	List<Dept> findDeptBySysId(@Param("sysId")String sysId);
}
