package com.students.model;
import lombok.*;

@Data
@NoArgsConstructor
public class GradeStore {

	int studentId;
	String studentName;
	long gradeSum;
	long gradeCount;

    private GradeStore(Builder builder) {
    	studentId = builder.studentId;
    	studentName = builder.studentName;
    	gradeSum = builder.gradeSum;
    	gradeCount = builder.gradeCount;
    }
    public long calcAvgGrade() { // NO getAvgGrade !!!
    	return gradeCount == 0 ? 0 : gradeSum/gradeCount;
    }
    
    public String toString() {
    	String result = "[studentId: " + studentId + "; studentName: " + studentName + "; avg grade: " + calcAvgGrade() + "]";
    	return result;
    }    
    public static Builder newBuilder() {
        return new Builder();
    }
    public static Builder newBuilder(GradeStore copy) {
        Builder builder = new Builder();
        builder.studentId = copy.studentId;
        builder.studentName = copy.studentName;
        builder.gradeSum = copy.gradeSum;
        builder.gradeCount = copy.gradeCount;
        return builder;
    }
    public static Builder newBuilder(Student student) {
        Builder builder = new Builder();
        builder.studentId = student.id;
        builder.studentName = student.name;
        builder.gradeSum = student.grade;
        builder.gradeCount = 1;
        return builder;
    }
	public static GradeStore sum (GradeStore st1, GradeStore st2) {
		int res = Integer.compare(st1.studentId, st2.studentId);
		if (res != 0) {
			return null;
		}
        Builder builder = newBuilder(st1);
        builder.gradeSum = st1.gradeSum + st2.gradeSum; 
        builder.gradeCount = st1.gradeCount + st2.gradeCount;
        return builder.build();		
	}
//===========================================================
    public static final class Builder {

    	private int studentId;
    	private String studentName;
    	private long gradeSum;
    	private long gradeCount;
	
        private Builder() { }

        public Builder withStudent(int val) {
        	studentId = val;
            return this;
        }
        public Builder withName(String val) {
        	studentName = val;
            return this;
        }
        
        public Builder withGradeSum(long val) {
        	gradeSum = val;
            return this;
        }
        public Builder withGradeCount(long val) {
        	gradeCount = val;
            return this;
        }
        public GradeStore build() {
            return new GradeStore(this);
        }
    }	
    
}
