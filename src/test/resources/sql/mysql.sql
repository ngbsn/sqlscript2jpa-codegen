CREATE TABLE organization.contractors (
    contractor_no   INT             NOT NULL,
    birth_date      DATE            NOT NULL,
    first_name      VARCHAR(14)     NOT NULL,
    last_name       VARCHAR(16)     NOT NULL,
    hire_date       DATE,
    PRIMARY KEY (contractor_no, first_name)
);

CREATE TABLE organization.employees (
    emp_no      INT             NOT NULL,
    birth_date  DATE            NOT NULL,
    first_name  VARCHAR(14)     NOT NULL,
    last_name   VARCHAR(16)     NOT NULL,
    hire_date   DATE            NOT NULL,
    grade ENUM('E1', 'E2', 'E3', 'M1', 'M2', 'M3'),
    PRIMARY KEY (emp_no)
);

CREATE TABLE organization.departments (
    dept_no     CHAR(4)         NOT NULL,
    dept_name   VARCHAR(40)     NOT NULL,
    PRIMARY KEY (dept_no),
    UNIQUE   	(dept_name)
);

CREATE TABLE organization.dept_manager (
   dept_no      CHAR(4)         NOT NULL,
   emp_no       INT             NOT NULL,
   FOREIGN KEY (emp_no)  REFERENCES organization.employees (emp_no)    ON DELETE CASCADE,
   FOREIGN KEY (dept_no) REFERENCES organization.departments (dept_no) ON DELETE CASCADE,
   PRIMARY KEY (emp_no,dept_no)
);

CREATE INDEX dept_manager_dept_no_idx ON organization.dept_manager(dept_no);

CREATE TABLE organization.dept_emp (
    emp_no      INT             NOT NULL,
    dept_no     CHAR(4)         NOT NULL,
    from_date   DATE            NOT NULL,
    to_date     DATE            NOT NULL,
    FOREIGN KEY (emp_no)  REFERENCES organization.employees   (emp_no)  ON DELETE CASCADE,
    FOREIGN KEY (dept_no) REFERENCES organization.departments (dept_no) ON DELETE CASCADE,
    PRIMARY KEY (emp_no,dept_no)
);

CREATE INDEX dept_emp_dept_no_idx ON organization.dept_emp(dept_no);

CREATE TABLE organization.titles (
    emp_no      INT             NOT NULL,
    title       VARCHAR(50)     NOT NULL,
    from_date   DATE            NOT NULL,
    to_date     DATE,
    FOREIGN KEY (emp_no) REFERENCES organization.employees (emp_no) ON DELETE CASCADE,
    PRIMARY KEY (title, from_date)
);


CREATE TABLE organization.salaries (
    emp_no      INT             NOT NULL,
    salary      INT             NOT NULL,
    from_date   DATE            NOT NULL,
    to_date     DATE            NOT NULL,
    FOREIGN KEY (emp_no) REFERENCES organization.employees (emp_no) ON DELETE CASCADE,
    PRIMARY KEY (emp_no, from_date)
);

CREATE TABLE organization.contractors_salaries (
    contractor_no   INT             NOT NULL,
    first_name      VARCHAR(14)     NOT NULL,
    salary          INT             NOT NULL,
    FOREIGN KEY (contractor_no, first_name) REFERENCES organization.contractors (contractor_no, first_name) ON DELETE CASCADE,
    PRIMARY KEY (contractor_no, first_name, salary)
);